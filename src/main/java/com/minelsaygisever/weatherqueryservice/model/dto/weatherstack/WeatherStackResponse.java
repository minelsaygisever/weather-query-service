package com.minelsaygisever.weatherqueryservice.model.dto.weatherstack;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherStackResponse(Current current) {
    public record Current(
            @JsonProperty("temperature") Integer temperature
    ) {}
}
