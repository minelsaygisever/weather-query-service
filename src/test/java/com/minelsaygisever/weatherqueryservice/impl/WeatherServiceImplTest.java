package com.minelsaygisever.weatherqueryservice.impl;

import com.minelsaygisever.weatherqueryservice.event.WeatherQuerySavedEvent;
import com.minelsaygisever.weatherqueryservice.exception.ExternalServiceException;
import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import com.minelsaygisever.weatherqueryservice.service.impl.WeatherServiceImpl;
import com.minelsaygisever.weatherqueryservice.service.provider.WeatherDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<WeatherQuerySavedEvent> eventCaptor;

    @BeforeEach
    void setUp() {
        Executor directExecutor = Runnable::run;

        weatherService = new WeatherServiceImpl(
                Arrays.asList(provider1, provider2),
                directExecutor,
                eventPublisher
        );

        lenient().when(provider1.getPriority()).thenReturn(1);
        lenient().when(provider2.getPriority()).thenReturn(2);
    }

    @Test
    void shouldCalculateAverage_AndPublishEvent_WhenBothProvidersSucceed() {
        // Arrange
        when(provider1.getCurrentTemperature("Istanbul")).thenReturn(10.0);
        when(provider2.getCurrentTemperature("Istanbul")).thenReturn(20.0);

        // Act
        WeatherResponse response = weatherService.getWeather("Istanbul", 5);

        // Assert Response
        assertEquals(15.0, response.temperature());
        assertEquals("Istanbul", response.location());

        // Assert Event Publishing
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        WeatherQuerySavedEvent capturedEvent = eventCaptor.getValue();

        assertEquals("Istanbul", capturedEvent.location());
        assertEquals(10.0, capturedEvent.service1Temp());
        assertEquals(20.0, capturedEvent.service2Temp());

        assertEquals(5, capturedEvent.requestCount());
    }

    @Test
    void shouldReturnSingleResult_AndPublishNullForFailed_WhenOneProviderFails() {
        // Arrange
        when(provider1.getCurrentTemperature("Istanbul")).thenReturn(10.0);
        when(provider2.getCurrentTemperature("Istanbul")).thenThrow(new RuntimeException("Connection Failed"));

        // Act
        WeatherResponse response = weatherService.getWeather("Istanbul", 1);

        // Assert Response
        assertEquals(10.0, response.temperature());

        // Assert Event
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        WeatherQuerySavedEvent capturedEvent = eventCaptor.getValue();

        assertEquals(10.0, capturedEvent.service1Temp()); // Provider 1
        assertNull(capturedEvent.service2Temp()); // Provider 2

        assertEquals(1, capturedEvent.requestCount());
    }

    @Test
    void shouldThrowException_AndNotPublishEvent_WhenAllProvidersFail() {
        // Arrange
        when(provider1.getCurrentTemperature("Istanbul")).thenThrow(new RuntimeException("Fail 1"));
        when(provider2.getCurrentTemperature("Istanbul")).thenThrow(new RuntimeException("Fail 2"));

        // Act & Assert Exception
        assertThrows(ExternalServiceException.class, () -> {
            weatherService.getWeather("Istanbul", 10);
        });

        verifyNoInteractions(eventPublisher);
    }
}
