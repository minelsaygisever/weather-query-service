package com.minelsaygisever.weatherqueryservice.event;

public record WeatherQuerySavedEvent(
        String location,
        Double service1Temp,
        Double service2Temp,
        Integer requestCount
) {}
