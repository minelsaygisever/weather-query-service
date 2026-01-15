package com.minelsaygisever.weatherqueryservice.service.provider;

import com.minelsaygisever.weatherqueryservice.model.dto.weatherstack.WeatherStackResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherStackProvider implements WeatherDataProvider {

    private final RestTemplate restTemplate;

    @Value("${weatherstack.api.url}")
    private String apiUrl;

    @Value("${weatherstack.api.key}")
    private String apiKey;

    @Override
    public Double getCurrentTemperature(String location) {
        String url = String.format("%s?access_key=%s&query=%s", apiUrl, apiKey, location);

        try {
            ResponseEntity<WeatherStackResponse> response = restTemplate.getForEntity(url, WeatherStackResponse.class);

            if (response.getBody() != null && response.getBody().current() != null) {
                Double temp = Double.valueOf(response.getBody().current().temperature());

                log.info("WeatherStack provider retrieved: {}C for {}", temp, location);
                return temp;
            }
        } catch (Exception e) {
            log.error("WeatherStack failed: {}", e.getMessage());
            return null;
        }
        return null;
    }
}
