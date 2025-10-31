package com.skyapi.weathernetworkapi.fullweather;

import com.skyapi.weathernetworkapi.realtimeweather.RealtimeWeatherDTO;

public class RealtimeWeatherFieldFilter {
    public boolean equals (Object object) {
        if (object instanceof RealtimeWeatherDTO) {
            RealtimeWeatherDTO realtimeWeatherDTO = (RealtimeWeatherDTO) object;
            System.out.println("CHeck RealtimeWeatherFieldFilter");
            return realtimeWeatherDTO.getStatus() == null;
        }
        return false;
    }
}
