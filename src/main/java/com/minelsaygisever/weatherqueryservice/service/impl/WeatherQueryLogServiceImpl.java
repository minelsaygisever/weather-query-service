package com.minelsaygisever.weatherqueryservice.service.impl;

import com.minelsaygisever.weatherqueryservice.model.dto.WeatherQueryLogResponse;
import com.minelsaygisever.weatherqueryservice.model.entity.WeatherQuery;
import com.minelsaygisever.weatherqueryservice.repository.WeatherQueryRepository;
import com.minelsaygisever.weatherqueryservice.service.WeatherQueryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeatherQueryLogServiceImpl implements WeatherQueryLogService {

    private final WeatherQueryRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<WeatherQueryLogResponse> getAllQueries(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::mapToDto);
    }

    private WeatherQueryLogResponse mapToDto(WeatherQuery entity) {
        return new WeatherQueryLogResponse(
                entity.getId(),
                entity.getLocation(),
                entity.getService1Temperature(),
                entity.getService2Temperature(),
                entity.getRequestCount(),
                entity.getCreatedAt()
        );
    }
}