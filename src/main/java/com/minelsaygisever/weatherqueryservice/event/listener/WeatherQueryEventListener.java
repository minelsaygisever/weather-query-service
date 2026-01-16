package com.minelsaygisever.weatherqueryservice.event.listener;

import com.minelsaygisever.weatherqueryservice.event.WeatherQuerySavedEvent;
import com.minelsaygisever.weatherqueryservice.model.entity.WeatherQuery;
import com.minelsaygisever.weatherqueryservice.repository.WeatherQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherQueryEventListener {

    private final WeatherQueryRepository repository;

    @Async
    @EventListener
    @Transactional
    public void handleWeatherQuerySavedEvent(WeatherQuerySavedEvent event) {
        log.info("Saving weather query event for location: {}", event.location());

        try {
            WeatherQuery entity = WeatherQuery.builder()
                    .location(event.location())
                    .service1Temperature(event.service1Temp())
                    .service2Temperature(event.service2Temp())
                    .requestCount(event.requestCount())
                    .build();

            repository.save(entity);
            log.info("Weather query saved successfully with ID: {}", entity.getId());
        } catch (Exception e) {
            log.error("Failed to save weather query log", e);
        }
    }
}