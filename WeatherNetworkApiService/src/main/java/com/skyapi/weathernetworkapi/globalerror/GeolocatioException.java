package com.skyapi.weathernetworkapi.globalerror;

public class GeolocatioException extends Exception {

    public GeolocatioException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeolocatioException(String message) {
        super(message);
    }
}
