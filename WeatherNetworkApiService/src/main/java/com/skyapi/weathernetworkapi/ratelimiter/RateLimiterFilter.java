package com.skyapi.weathernetworkapi.ratelimiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/*Global filter that applies Rate Limiting per client*/
@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterFilter.class);

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/v1")) {
            String IPAddress = request.getRemoteAddr();
            LOGGER.info("IPAddress: {}", IPAddress);
            Bucket bucket = buckets.computeIfAbsent(IPAddress, this::newBucket);
            if(bucket.tryConsume(1)){
                filterChain.doFilter(request, response);
            } else {
                LOGGER.info("Too many requests from IP: " + IPAddress);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().println("Too many requests from IP: " + IPAddress);
            }
        }else {
            filterChain.doFilter(request, response);
        }
    }

    /*Create new Rate Limiting Bucket for given IP.Allows 5 request per 30*/
    private Bucket newBucket(String ipAddress) {
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration. ofSeconds(60)));
        return Bucket4j.builder().addLimit(limit).build();
    }
}
