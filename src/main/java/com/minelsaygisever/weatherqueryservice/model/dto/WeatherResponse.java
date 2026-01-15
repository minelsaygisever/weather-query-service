package com.minelsaygisever.weatherqueryservice.model.dto;

import java.io.Serializable;

public record WeatherResponse(
        String location,
        double temperature
) implements Serializable {}
