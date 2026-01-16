package com.minelsaygisever.weatherqueryservice.controller;

import com.minelsaygisever.weatherqueryservice.controller.api.WeatherSimulationApi;
import com.minelsaygisever.weatherqueryservice.service.aggregator.WeatherAggregatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WeatherSimulationController implements WeatherSimulationApi {

    private final WeatherAggregatorService aggregatorService;

    private final ExecutorService simulationExecutor = Executors.newFixedThreadPool(50);

    @Override
    public ResponseEntity<String> triggerLoadTest(String city, int requestCount) {
        log.info("Starting load test for {} with {} requests...", city, requestCount);

        IntStream.range(0, requestCount).forEach(i -> {
            simulationExecutor.submit(() -> {
                try {
                    aggregatorService.getWeather(city)
                            .thenAccept(response ->
                                    log.debug("User-{} received: {}°C", i, response.temperature())
                            )
                            .exceptionally(ex -> {
                                log.error("User-{} failed: {}", i, ex.getMessage());
                                return null;
                            });

                } catch (Exception e) {
                    log.error("Simulation dispatch error", e);
                }
            });
        });

        return ResponseEntity.ok(String.format("Load test started for %s with %d requests. Check logs!", city, requestCount));
    }
}