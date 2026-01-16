package com.minelsaygisever.weatherqueryservice.integration;

import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import com.minelsaygisever.weatherqueryservice.service.WeatherService;
import com.minelsaygisever.weatherqueryservice.service.aggregator.WeatherAggregatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
public class WeatherAggregatorIntegrationTest {

    @Autowired
    private WeatherAggregatorService aggregatorService;

    @MockitoBean
    private WeatherService weatherService;

    @Test
    void shouldTriggerImmediately_WhenBatchSizeIsReached() throws ExecutionException, InterruptedException {
        // Arrange
        String city = "Istanbul";
        WeatherResponse mockResponse = new WeatherResponse(city, 25.0);

        when(weatherService.getWeather(eq(city), anyInt())).thenReturn(mockResponse);

        List<CompletableFuture<WeatherResponse>> futures = new ArrayList<>();

        // Act: Send 10 requests to fill the batch capacity
        for (int i = 0; i < 10; i++) {
            futures.add(aggregatorService.getWeather(city));
        }

        // Wait for all futures to complete (since batch is full, it should process immediately)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Assert
        for (CompletableFuture<WeatherResponse> future : futures) {
            assertThat(future.get()).isNotNull();
            assertThat(future.get().temperature()).isEqualTo(25.0);
        }

        // Verify that the service was called EXACTLY ONCE with a batch size of 10
        verify(weatherService, times(1)).getWeather(city, 10);
    }

    @Test
    void shouldTriggerByTimeout_WhenBatchSizeIsNotReached() throws InterruptedException {
        // Arrange
        String city = "Ankara";
        WeatherResponse mockResponse = new WeatherResponse(city, 15.0);
        when(weatherService.getWeather(eq(city), anyInt())).thenReturn(mockResponse);

        // Act: Send only 3 requests (below batch limit of 10)
        aggregatorService.getWeather(city);
        aggregatorService.getWeather(city);
        aggregatorService.getWeather(city);

        // Assert 1: Ensure service is NOT called immediately
        verify(weatherService, never()).getWeather(anyString(), anyInt());

        // Wait for the timeout period (5000ms) plus a small buffer (500ms)
        // to allow the scheduler to trigger the process.
        Thread.sleep(5500);

        // Assert 2: Verify that service is called after timeout with the accumulated count (3)
        verify(weatherService, times(1)).getWeather(city, 3);
    }

    @Test
    void shouldProcessSeparately_WhenEleventhRequestArrives() {
        // Arrange
        String city = "Izmir";
        WeatherResponse mockResponse = new WeatherResponse(city, 30.0);
        when(weatherService.getWeather(eq(city), anyInt())).thenReturn(mockResponse);

        // Act: Send 11 requests (10 for the first batch, 1 for the new batch)
        for (int i = 0; i < 11; i++) {
            aggregatorService.getWeather(city);
        }

        // Give a small delay to ensure the async processing of the first batch (10) starts
        try { Thread.sleep(100); } catch (InterruptedException e) {}

        // Assert
        // The first batch of 10 should be processed immediately
        verify(weatherService, times(1)).getWeather(city, 10);

        // The 11th request should be pending in a new batch (waiting for timeout or full capacity)
        verify(weatherService, never()).getWeather(city, 1);
    }
}