package com.minelsaygisever.weatherqueryservice.service;

import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;

public interface WeatherService {
    WeatherResponse getWeather(String location, int requestCount);
}
