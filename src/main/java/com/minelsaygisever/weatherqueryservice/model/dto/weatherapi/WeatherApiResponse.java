package com.minelsaygisever.weatherqueryservice.model.dto.weatherapi;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WeatherApiResponse(
        Location location,
        Current current
) {

    public record Location(
            String name,
            String region,
            String country
    ) {}

    public record Current(
            @JsonProperty("temp_c")
            Double tempC,

            @JsonProperty("condition")
            Condition condition
    ) {}
    public record Condition(
            String text,
            String icon
    ) {}
}