package com.minelsaygisever.weatherqueryservice.service.impl;

import com.minelsaygisever.weatherqueryservice.event.WeatherQuerySavedEvent;
import com.minelsaygisever.weatherqueryservice.exception.ExternalServiceException;
import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import com.minelsaygisever.weatherqueryservice.service.WeatherService;
import com.minelsaygisever.weatherqueryservice.service.provider.WeatherDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final List<WeatherDataProvider> weatherProviders;
    private final Executor taskExecutor;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public WeatherResponse getWeather(String location) {
        log.info("Getting weather for {} asynchronously", location);

        Map<Integer, Double> results = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = weatherProviders.stream()
                .map(provider -> CompletableFuture.runAsync(() -> {
                    try {
                        Double temp = provider.getCurrentTemperature(location);
                        if (temp != null) {
                            results.put(provider.getPriority(), temp);
                        }
                    } catch (Exception e) {
                        log.warn("Provider {} failed for location {}: {}", provider.getProviderName(), location, e.getMessage());
                    }
                }, taskExecutor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        double totalTemp = results.values().stream().mapToDouble(Double::doubleValue).sum();
        double averageTemp = totalTemp / results.size();

        log.info("Average temperature for {}: {}", location, averageTemp);

        if (results.isEmpty()) {
            throw new ExternalServiceException("All weather providers failed for location: " + location);
        }

        publishQueryEvent(location, results);

        return new WeatherResponse(location, averageTemp);
    }

    private void publishQueryEvent(String location, Map<Integer, Double> results) {
        Double temp1 = results.get(1);
        Double temp2 = results.get(2);

        // TODO: Request count = 1 for now
        WeatherQuerySavedEvent event = new WeatherQuerySavedEvent(location, temp1, temp2, 1);

        eventPublisher.publishEvent(event);
    }
}