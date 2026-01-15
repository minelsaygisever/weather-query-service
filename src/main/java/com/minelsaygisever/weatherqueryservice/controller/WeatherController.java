package com.minelsaygisever.weatherqueryservice.controller;

import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import com.minelsaygisever.weatherqueryservice.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WeatherController implements WeatherApi {

    private final WeatherService weatherService;

    @Override
    public ResponseEntity<WeatherResponse> getWeather(String location) {
        WeatherResponse response = weatherService.getWeather(location);
        return ResponseEntity.ok(response);
    }
}