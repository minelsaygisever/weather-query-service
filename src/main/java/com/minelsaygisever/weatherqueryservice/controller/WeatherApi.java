package com.minelsaygisever.weatherqueryservice.controller;

import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/weather")
@Validated
@Tag(name = "Weather API", description = "Operations related to weather queries")
public interface WeatherApi {

    @Operation(
            summary = "Get weather by location",
            description = "Fetches the current weather for a given city name.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation",
                            content = @Content(schema = @Schema(implementation = WeatherResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid location parameter")
            }
    )
    @GetMapping
    ResponseEntity<WeatherResponse> getWeather(
            @Parameter(description = "City name (e.g. Istanbul)", required = true, example = "Istanbul")
            @RequestParam("q")
            @NotBlank(message = "Location parameter cannot be empty")
            String location
    );
}