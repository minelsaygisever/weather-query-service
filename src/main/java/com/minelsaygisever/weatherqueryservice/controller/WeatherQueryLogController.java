package com.minelsaygisever.weatherqueryservice.controller;

import com.minelsaygisever.weatherqueryservice.controller.api.WeatherQueryLogApi;
import com.minelsaygisever.weatherqueryservice.model.dto.WeatherQueryLogResponse;
import com.minelsaygisever.weatherqueryservice.service.WeatherQueryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WeatherQueryLogController implements WeatherQueryLogApi {

    private final WeatherQueryLogService historyService;

    @Override
    public ResponseEntity<Page<WeatherQueryLogResponse>> getQueryLogs(Pageable pageable) {
        return ResponseEntity.ok(historyService.getAllQueries(pageable));
    }
}
