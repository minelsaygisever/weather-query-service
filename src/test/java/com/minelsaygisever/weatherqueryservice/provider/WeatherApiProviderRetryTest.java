package com.minelsaygisever.weatherqueryservice.provider;

import com.minelsaygisever.weatherqueryservice.model.dto.weatherapi.WeatherApiResponse;
import com.minelsaygisever.weatherqueryservice.service.provider.WeatherApiProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class WeatherApiProviderRetryTest {

    @Autowired
    private WeatherApiProvider weatherApiProvider;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void shouldRetryThreeTimes_WhenApiFails() {
        when(restTemplate.getForEntity(anyString(), eq(WeatherApiResponse.class)))
                .thenThrow(new ResourceAccessException("Timeout"));

        assertThrows(Exception.class, () -> {
            weatherApiProvider.getCurrentTemperature("London");
        });

        verify(restTemplate, times(3)).getForEntity(anyString(), eq(WeatherApiResponse.class));
    }
}