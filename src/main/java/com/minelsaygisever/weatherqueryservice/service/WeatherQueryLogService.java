package com.minelsaygisever.weatherqueryservice.service;

import com.minelsaygisever.weatherqueryservice.model.dto.WeatherQueryLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WeatherQueryLogService {
    Page<WeatherQueryLogResponse> getAllQueries(Pageable pageable);
}
