package com.minelsaygisever.weatherqueryservice.repository;

import com.minelsaygisever.weatherqueryservice.model.entity.WeatherQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherQueryRepository extends JpaRepository<WeatherQuery, Long> {
}
