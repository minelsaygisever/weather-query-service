package com.minelsaygisever.weatherqueryservice.controller;

import com.minelsaygisever.weatherqueryservice.controller.api.WeatherApi;
import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import com.minelsaygisever.weatherqueryservice.service.aggregator.WeatherAggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
public class WeatherController implements WeatherApi {

    private final WeatherAggregatorService weatherAggregatorService;

    @Override
    public CompletableFuture<ResponseEntity<WeatherResponse>> getWeather(String location) {
        return weatherAggregatorService.getWeather(location)
                .thenApply(ResponseEntity::ok);
    }
}