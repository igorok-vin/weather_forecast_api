package com.skyapi.weathernetworkapi;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CommonUtility {
    public static Logger LOGGER = LoggerFactory.getLogger(CommonUtility.class);

    public static String getIPAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-FORWARDED-FOR");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        LOGGER.info("Client's IP Address: " + ip);
        return ip;
    }

    public static String responseHeaderDateAndTime() {
        Instant now = Instant.now();
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(now, ZoneId.of("UTC-06:00"));
        DateTimeFormatter formatter2 = DateTimeFormatter.RFC_1123_DATE_TIME;
        String formattedDate = zonedDateTime.format(formatter2);
        return formattedDate;
    }
}
