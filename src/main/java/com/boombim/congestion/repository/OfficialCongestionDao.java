package com.boombim.congestion.repository;

import com.boombim.openapi.dto.OpenApiResponse.CityDataItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OfficialCongestionDao {

    private final JdbcTemplate jdbcTemplate;

    public void saveAll(
        List<CityDataItem> items
    ) {
        String sql = """
            INSERT INTO official_congestion (
                poi_code, congestion_level, congestion_message,
                population_min, population_max,
                male_population_rate, female_population_rate,
                population_rate_0, population_rate_10, population_rate_20, population_rate_30,
                population_rate_40, population_rate_50, population_rate_60, population_rate_70,
                resident_population_rate, non_resident_population_rate,
                observed_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        List<Object[]> batchArgs = items.stream()
            .map(item -> new Object[]{
                item.areaCode(),
                item.congestionLevel(),
                item.congestionMessage(),
                item.populationMinimum(),
                item.populationMaximum(),
                item.malePopulationRate(),
                item.femalePopulationRate(),
                item.populationRate0(),
                item.populationRate10(),
                item.populationRate20(),
                item.populationRate30(),
                item.populationRate40(),
                item.populationRate50(),
                item.populationRate60(),
                item.populationRate70(),
                item.residentPopulationRate(),
                item.nonResidentPopulationRate(),
                item.populationTime()
            })
            .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);

    }

}
