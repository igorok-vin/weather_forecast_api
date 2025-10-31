package com.skyapi.weathernetworkapi.globalerror;

public class BadRequestException extends Exception {
    public BadRequestException(String message) {
        super(message);
    }
}
