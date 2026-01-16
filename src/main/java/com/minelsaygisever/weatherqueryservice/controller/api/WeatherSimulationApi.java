package com.minelsaygisever.weatherqueryservice.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/simulation")
@Tag(name = "Simulation API", description = "Load testing tools for batch verification")
public interface WeatherSimulationApi {

    @Operation(
            summary = "Trigger Batch Load Test",
            description = "Simulates concurrent user requests to test aggregation logic. Sends N requests in parallel."
    )
    @PostMapping("/load-test")
    ResponseEntity<String> triggerLoadTest(
            @Parameter(description = "City name to query", example = "Istanbul")
            @RequestParam(defaultValue = "Istanbul") String city,

            @Parameter(description = "Number of concurrent requests", example = "25")
            @RequestParam(defaultValue = "25") int requestCount
    );
}