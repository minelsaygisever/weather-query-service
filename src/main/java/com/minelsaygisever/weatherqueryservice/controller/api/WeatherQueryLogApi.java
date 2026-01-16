package com.minelsaygisever.weatherqueryservice.controller.api;

import com.minelsaygisever.weatherqueryservice.model.dto.WeatherQueryLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/query-logs")
@Tag(name = "Query Logs", description = "Access to system query logs and audit data")
public interface WeatherQueryLogApi {

    @Operation(
            summary = "Get query logs",
            description = "Retrieves a paginated list of past queries made to the system."
    )
    @GetMapping
    ResponseEntity<Page<WeatherQueryLogResponse>> getQueryLogs(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    );
}