package com.minelsaygisever.weatherqueryservice.impl;

import com.minelsaygisever.weatherqueryservice.exception.ExternalServiceException;
import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import com.minelsaygisever.weatherqueryservice.service.impl.WeatherServiceImpl;
import com.minelsaygisever.weatherqueryservice.service.provider.WeatherDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WeatherServiceImplTest {
    private WeatherServiceImpl weatherService;

    @Mock
    private WeatherDataProvider provider1;

    @Mock
    private WeatherDataProvider provider2;

    @BeforeEach
    void setUp() {
        Executor directExecutor = Runnable::run;

        weatherService = new WeatherServiceImpl(
                Arrays.asList(provider1, provider2),
                directExecutor
        );
    }

    @Test
    void shouldCalculateAverage_WhenBothProvidersSucceed() {
        when(provider1.getCurrentTemperature("Istanbul")).thenReturn(10.0);
        when(provider2.getCurrentTemperature("Istanbul")).thenReturn(20.0);

        WeatherResponse response = weatherService.getWeather("Istanbul");

        assertEquals(15.0, response.temperature());
        assertEquals("Istanbul", response.location());
    }

    @Test
    void shouldReturnSingleResult_WhenOneProviderFails() {
        when(provider1.getCurrentTemperature("Istanbul")).thenReturn(10.0);
        when(provider2.getCurrentTemperature("Istanbul")).thenThrow(new RuntimeException("Connection Failed"));

        WeatherResponse response = weatherService.getWeather("Istanbul");

        assertEquals(10.0, response.temperature());
    }

    @Test
    void shouldThrowException_WhenAllProvidersFail() {
        when(provider1.getCurrentTemperature("Istanbul")).thenThrow(new RuntimeException("Fail 1"));
        when(provider2.getCurrentTemperature("Istanbul")).thenThrow(new RuntimeException("Fail 2"));

        assertThrows(ExternalServiceException.class, () -> {
            weatherService.getWeather("Istanbul");
        });
    }
}
