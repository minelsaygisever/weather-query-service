package com.minelsaygisever.weatherqueryservice.service.impl;

import com.minelsaygisever.weatherqueryservice.exception.ExternalServiceException;
import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import com.minelsaygisever.weatherqueryservice.service.WeatherService;
import com.minelsaygisever.weatherqueryservice.service.provider.WeatherDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final List<WeatherDataProvider> weatherProviders;
    private final Executor taskExecutor;

    @Override
    public WeatherResponse getWeather(String location) {
        log.info("Getting weather for {} asynchronously", location);

        List<CompletableFuture<Double>> futures = weatherProviders.stream()
                .map(provider -> CompletableFuture.supplyAsync(() -> {
                    return provider.getCurrentTemperature(location);
                }, taskExecutor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        double totalTemp = 0.0;
        int successfulProviders = 0;

        for (CompletableFuture<Double> future : futures) {
            try {
                Double temp = future.get();
                if (temp != null) {
                    totalTemp += temp;
                    successfulProviders++;
                }
            } catch (Exception e) {
                log.warn("One provider failed during async execution", e);
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