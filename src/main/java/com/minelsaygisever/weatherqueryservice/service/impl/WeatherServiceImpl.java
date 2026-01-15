package com.minelsaygisever.weatherqueryservice.service.impl;

import com.minelsaygisever.weatherqueryservice.exception.ExternalServiceException;
import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import com.minelsaygisever.weatherqueryservice.service.WeatherService;
import com.minelsaygisever.weatherqueryservice.service.provider.WeatherDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final List<WeatherDataProvider> weatherProviders;

    @Override
    public WeatherResponse getWeather(String location) {
        log.info("Getting weather for {} from {} providers", location, weatherProviders.size());

        double totalTemp = 0.0;
        int successfulProviders = 0;

        for (WeatherDataProvider provider : weatherProviders) {
            Double temp = provider.getCurrentTemperature(location);
            if (temp != null) {
                totalTemp += temp;
                successfulProviders++;
            }
        }

        if (successfulProviders == 0) {
            throw new ExternalServiceException("All weather providers failed for location: " + location);
        }

        double averageTemp = totalTemp / successfulProviders;
        log.info("Average temperature for {}: {}", location, averageTemp);

        return new WeatherResponse(location, averageTemp);
    }
}