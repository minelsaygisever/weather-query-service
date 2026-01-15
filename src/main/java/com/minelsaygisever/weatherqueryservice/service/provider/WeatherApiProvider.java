package com.minelsaygisever.weatherqueryservice.service.provider;

import com.minelsaygisever.weatherqueryservice.model.dto.weatherapi.WeatherApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherApiProvider implements WeatherDataProvider {

    private final RestTemplate restTemplate;

    @Value("${weather.api.url}")
    private String apiUrl;

    @Value("${weather.api.key}")
    private String apiKey;

    @Override
    public Double getCurrentTemperature(String location) {
        String url = String.format("%s?key=%s&q=%s&days=1&aqi=no&alerts=no", apiUrl, apiKey, location);

        try {
            ResponseEntity<WeatherApiResponse> response = restTemplate.getForEntity(url, WeatherApiResponse.class);

            if (response.getBody() != null && response.getBody().current() != null) {
                Double temp = response.getBody().current().tempC();
                log.info("WeatherAPI provider retrieved: {}C for {}", temp, location);
                return temp;
            }
        } catch (Exception e) {
            log.error("WeatherAPI failed: {}", e.getMessage());
            return null;
        }
        return null;
    }
}