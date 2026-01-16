package com.minelsaygisever.weatherqueryservice.model.dto;

import java.time.LocalDateTime;

public record WeatherQueryLogResponse(
        Long id,
        String location,
        Double service1Temp,
        Double service2Temp,
        Integer requestCount,
        LocalDateTime createdAt
) {}
