package com.minelsaygisever.weatherqueryservice.service.impl;

import com.minelsaygisever.weatherqueryservice.exception.ExternalServiceException;
import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import com.minelsaygisever.weatherqueryservice.model.dto.weatherapi.WeatherApiResponse;
import com.minelsaygisever.weatherqueryservice.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final RestTemplate restTemplate;

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    @Override
    public WeatherResponse getWeather(String location) {
        log.info("Requesting weather data from WeatherAPI for location: {}", location);

        String url = String.format("%s?key=%s&q=%s&days=1&aqi=no&alerts=no", apiUrl, apiKey, location);

        try {
            ResponseEntity<WeatherApiResponse> response = restTemplate.getForEntity(url, WeatherApiResponse.class);

            if (response.getBody() != null && response.getBody().current() != null) {
                Double tempC = response.getBody().current().tempC();

                log.info("WeatherAPI returned temperature: {}C for {}", tempC, location);

                return new WeatherResponse(location, tempC);
            }
        } catch (Exception e) {
            log.error("Error fetching data from WeatherAPI: {}", e.getMessage());
            throw new ExternalServiceException("Failed to fetch weather data: " + e.getMessage());
        }

        throw new ExternalServiceException("Weather data not found or API response invalid.");
    }
}