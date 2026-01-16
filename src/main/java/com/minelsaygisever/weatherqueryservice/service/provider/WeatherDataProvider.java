package com.minelsaygisever.weatherqueryservice.service.provider;

public interface WeatherDataProvider {
    String getProviderName();
    Double getCurrentTemperature(String location);
    int getPriority();
}
