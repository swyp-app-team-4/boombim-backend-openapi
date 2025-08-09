package com.boombim.congestion.repository;

import com.boombim.openapi.dto.OpenApiResponse.CityDataItem.ForecastItem;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OfficialCongestionForecastDao {

    private final JdbcTemplate jdbcTemplate;

    public void saveAll(
        List<ForecastItem> items,
        LocalDateTime observedAt,
        String poiCode
    ) {

        String sql = """
            INSERT INTO official_congestion_forecast (
                poi_code, observed_at, forecast_time, forecast_congestion_level,
                forecast_population_min, forecast_population_max
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        List<Object[]> batchArgs = items.stream()
            .map(item -> new Object[]{
                poiCode,
                observedAt,
                item.forecastTime(),
                item.forecastCongestLevel(),
                item.forecastPopulationMinimum(),
                item.forecastPopulationMaximum()
            })
            .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

}
