package com.boombim.congestion.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OfficialCongestionForecastDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 예측 데이터를 일괄 저장
     *
     * @param batchArgs 서비스 계층에서 모든 파라미터가 준비된 Object 배열 리스트
     */
    public void saveAll(List<Object[]> batchArgs) {
        if (batchArgs == null || batchArgs.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO official_congestion_forecasts (
                poi_code, observed_at, forecast_time, forecast_congestion_level_id,
                forecast_population_min, forecast_population_max
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}
