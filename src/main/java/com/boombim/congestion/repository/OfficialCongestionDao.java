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
     * @param poiCode           장소 코드
     * @param congestionLevelId 혼잡도 수준 ID
     * @param populationMin     최소 인구
     * @param populationMax     최대 인구
     * @param observedAt        관측 시각
     * @return 생성된 official_congestions 테이블의 id
     */
    public Long save(
        Long officialPlaceId,
        Integer congestionLevelId,
        Long populationMin,
        Long populationMax,
        LocalDateTime observedAt
    ) {

        String sql = """
            INSERT INTO official_congestions (
                official_place_id, congestion_level_id, population_min, population_max, observed_at
            ) VALUES (?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, officialPlaceId);
            ps.setInt(2, congestionLevelId);
            ps.setObject(3, populationMin);
            ps.setObject(4, populationMax);
            ps.setObject(5, observedAt);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to retrieve generated key for official_congestions.");
        }

        return key.longValue();
    }
}
