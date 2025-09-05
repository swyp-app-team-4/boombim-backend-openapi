package com.boombim.congestion.repository;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OfficialCongestionDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 하나의 혼잡도 데이터를 저장하고, 자동 생성된 PK(id)를 반환
     *
     * @param officialPlaceId   공식 장소 ID
     * @param congestionLevelId 혼잡도 수준 ID
     * @param populationMin     최소 인구 (NULL 아님)
     * @param populationMax     최대 인구 (NULL 아님)
     * @param observedAt        관측 시각 (NULL 아님)
     * @return 생성된 official_congestions.id
     */
    public Long save(
        Long officialPlaceId,
        Integer congestionLevelId,
        Long populationMin,
        Long populationMax,
        LocalDateTime observedAt
    ) {

        final String sql = """
            INSERT INTO official_congestions (
                official_place_id,
                congestion_level_id,
                population_min,
                population_max,
                observed_at,
                density_per_m2
            )
            SELECT
                ?, ?, ?, ?, ?,
                CASE
                    WHEN op.area_m2 IS NOT NULL AND op.area_m2 > 0
                      THEN (COALESCE(?, ?)::double precision) / op.area_m2
                    ELSE NULL
                END
            FROM official_places op
            WHERE op.id = ?
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[]{"id"});

            ps.setLong(1, officialPlaceId);
            ps.setInt(2, congestionLevelId);
            ps.setLong(3, populationMin);
            ps.setLong(4, populationMax);
            ps.setObject(5, observedAt);

            ps.setLong(6, populationMax);
            ps.setLong(7, populationMin);

            ps.setLong(8, officialPlaceId);

            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to retrieve generated key for official_congestions.");
        }
        return key.longValue();
    }
}
