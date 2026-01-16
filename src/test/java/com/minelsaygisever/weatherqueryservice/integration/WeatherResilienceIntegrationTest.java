package com.minelsaygisever.weatherqueryservice.integration;

import com.minelsaygisever.weatherqueryservice.service.WeatherService;
import com.minelsaygisever.weatherqueryservice.service.provider.WeatherApiProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WeatherResilienceIntegrationTest {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private WeatherApiProvider weatherApiProvider;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void shouldOpenCircuitBreaker_WhenFailureRateExceeded() {
        // Arrange
        when(restTemplate.getForEntity(anyString(), any()))
                .thenThrow(new ResourceAccessException("Connection Timeout"));

        // Act
        // Sliding window size = 5
        for (int i = 0; i < 5; i++) {
            try {
                weatherService.getWeather("Istanbul", 1);
            } catch (Exception e) {

            }
        }

        // Assert
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("weatherCircuitBreaker");

        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void shouldRetryThreeTimes_WhenApiFails() {
        // Arrange
        when(restTemplate.getForEntity(anyString(), any()))
                .thenThrow(new ResourceAccessException("Timeout"));

        // Act & Assert
        assertThrows(Exception.class, () -> {
            weatherApiProvider.getCurrentTemperature("London");
        });

        // Verify
        verify(restTemplate, times(3)).getForEntity(anyString(), any());
    }
}
