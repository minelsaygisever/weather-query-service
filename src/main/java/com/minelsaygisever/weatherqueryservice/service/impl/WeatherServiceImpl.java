package com.minelsaygisever.weatherqueryservice.service.impl;

import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import com.minelsaygisever.weatherqueryservice.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {

    @Override
    public WeatherResponse getWeather(String location) {
        log.info("Mock weather request received for location: {}", location);

        // TODO: Returning mock data
        return new WeatherResponse(location, 25.0);
    }
}