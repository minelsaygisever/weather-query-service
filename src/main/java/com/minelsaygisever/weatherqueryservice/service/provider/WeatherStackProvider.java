package com.minelsaygisever.weatherqueryservice.service.provider;

import com.minelsaygisever.weatherqueryservice.model.dto.weatherstack.WeatherStackResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
public class WeatherStackProvider implements WeatherDataProvider {

    private final RestTemplate restTemplate;

    @Value("${weatherstack.api.url}")
    private String apiUrl;

    @Value("${weatherstack.api.key}")
    private String apiKey;

    @Override
    public String getProviderName() {
        return "WeatherStack";
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    @Retry(name = "weatherRetry")
    @CircuitBreaker(name = "weatherCircuitBreaker")
    public Double getCurrentTemperature(String location) {
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("access_key", apiKey)
                .queryParam("query", location)
                .toUriString();

        ResponseEntity<WeatherStackResponse> response = restTemplate.getForEntity(url, WeatherStackResponse.class);
        if (response.getBody() != null && response.getBody().current() != null) {
            Double temp = Double.valueOf(response.getBody().current().temperature());

            log.info("WeatherStack provider retrieved: {}C for {}", temp, location);
            return temp;
        }

        return null;
    }
}
