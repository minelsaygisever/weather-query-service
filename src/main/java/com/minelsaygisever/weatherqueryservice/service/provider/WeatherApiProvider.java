package com.minelsaygisever.weatherqueryservice.service.provider;

import com.minelsaygisever.weatherqueryservice.model.dto.weatherapi.WeatherApiResponse;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
    public String getProviderName() {
        return "WeatherAPI";
    }

    @Override
    @Retry(name = "weatherRetry")
    public Double getCurrentTemperature(String location) {
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("key", apiKey)
                .queryParam("q", location)
                .queryParam("days", 1)
                .queryParam("aqi", "no")
                .queryParam("alerts", "no")
                .toUriString();

        ResponseEntity<WeatherApiResponse> response = restTemplate.getForEntity(url, WeatherApiResponse.class);
        if (response.getBody() != null && response.getBody().current() != null) {
            Double temp = response.getBody().current().tempC();
            log.info("WeatherAPI provider retrieved: {}C for {}", temp, location);
            return temp;
        }
        return null;
    }
}