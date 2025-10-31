package com.skyapi.weathernetworkapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("production")
public class ResourceServerConfig {

    private static final String LOCATION_ENDPOINT_PATTERN = "/v1/locations/**";
    private static final String REALTIME_ENDPOINT_PATTERN = "/v1/realtime/**";
    private static final String HOURLY_ENDPOINT_PATTERN = "/v1/hourly/**";
    private static final String DAILY_ENDPOINT_PATTERN = "/v1/daily/**";
    private static final String FULL_ENDPOINT_PATTERN = "/v1/full/**";

    @Bean
    SecurityFilterChain securityFilterChainForResourceServer(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests.requestMatchers("/").permitAll()
                        .requestMatchers("/api/v1/test").permitAll()

                        .requestMatchers(HttpMethod.GET, LOCATION_ENDPOINT_PATTERN).hasAnyAuthority("SCOPE_SYSTEM", "SCOPE_READER")
                        .requestMatchers(HttpMethod.POST, LOCATION_ENDPOINT_PATTERN).hasAuthority("SCOPE_SYSTEM")
                        .requestMatchers(HttpMethod.DELETE, LOCATION_ENDPOINT_PATTERN).hasAuthority("SCOPE_SYSTEM")
                        .requestMatchers(HttpMethod.PUT, LOCATION_ENDPOINT_PATTERN).hasAuthority("SCOPE_SYSTEM")

                        .requestMatchers(HttpMethod.GET, REALTIME_ENDPOINT_PATTERN).hasAnyAuthority("SCOPE_UPDATER","SCOPE_SYSTEM", "SCOPE_READER")
                        .requestMatchers(HttpMethod.PUT, REALTIME_ENDPOINT_PATTERN).hasAnyAuthority("SCOPE_UPDATER","SCOPE_SYSTEM")

                        .requestMatchers(HttpMethod.GET, HOURLY_ENDPOINT_PATTERN).hasAnyAuthority("SCOPE_UPDATER","SCOPE_SYSTEM", "SCOPE_READER")
                        .requestMatchers(HttpMethod.PUT, HOURLY_ENDPOINT_PATTERN).hasAnyAuthority("SCOPE_UPDATER","SCOPE_SYSTEM")

                        .requestMatchers(HttpMethod.GET, DAILY_ENDPOINT_PATTERN).hasAnyAuthority("SCOPE_UPDATER","SCOPE_SYSTEM", "SCOPE_READER")
                        .requestMatchers(HttpMethod.PUT, DAILY_ENDPOINT_PATTERN).hasAnyAuthority("SCOPE_UPDATER","SCOPE_SYSTEM")

                        .requestMatchers(HttpMethod.GET, FULL_ENDPOINT_PATTERN).hasAnyAuthority("SCOPE_UPDATER","SCOPE_SYSTEM", "SCOPE_READER")
                        .requestMatchers(HttpMethod.PUT, FULL_ENDPOINT_PATTERN).hasAnyAuthority("SCOPE_UPDATER","SCOPE_SYSTEM")
                        .anyRequest().authenticated());
        return http.build();
    }
}
