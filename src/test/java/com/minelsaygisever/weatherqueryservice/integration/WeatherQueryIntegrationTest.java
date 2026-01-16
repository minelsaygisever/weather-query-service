package com.minelsaygisever.weatherqueryservice.integration;

import com.minelsaygisever.weatherqueryservice.model.entity.WeatherQuery;
import com.minelsaygisever.weatherqueryservice.repository.WeatherQueryRepository;
import com.minelsaygisever.weatherqueryservice.service.WeatherService;
import com.minelsaygisever.weatherqueryservice.service.provider.WeatherApiProvider;
import com.minelsaygisever.weatherqueryservice.service.provider.WeatherStackProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class WeatherQueryIntegrationTest {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private WeatherQueryRepository weatherQueryRepository;

    @MockitoBean
    private WeatherApiProvider weatherApiProvider;

    @MockitoBean
    private WeatherStackProvider weatherStackProvider;

    @BeforeEach
    void setUp() {
        weatherQueryRepository.deleteAll();
    }

    @Test
    void shouldSaveWeatherQueryToDatabase_WhenServiceReturnsResult() {
        // Arrange
        when(weatherApiProvider.getProviderName()).thenReturn("WeatherAPI");
        when(weatherApiProvider.getPriority()).thenReturn(1);
        when(weatherApiProvider.getCurrentTemperature("London")).thenReturn(15.0);

        when(weatherStackProvider.getProviderName()).thenReturn("WeatherStack");
        when(weatherStackProvider.getPriority()).thenReturn(2);
        when(weatherStackProvider.getCurrentTemperature("London")).thenReturn(17.0);

        // Act
        weatherService.getWeather("London", 5);

        // Wait for Async
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // Assert
        List<WeatherQuery> logs = weatherQueryRepository.findAll();

        assertThat(logs).hasSize(1);
        WeatherQuery log = logs.get(0);

        assertThat(log.getLocation()).isEqualTo("London");
        assertThat(log.getService1Temperature()).isEqualTo(15.0); // WeatherAPI (Priority 1)
        assertThat(log.getService2Temperature()).isEqualTo(17.0); // WeatherStack (Priority 2)
        assertThat(log.getRequestCount()).isEqualTo(5);
    }
}
