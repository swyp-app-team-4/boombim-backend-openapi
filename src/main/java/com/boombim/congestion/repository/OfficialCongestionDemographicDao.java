package com.boombim.congestion.repository;

import com.boombim.congestion.dto.OfficialCongestionDemographicInfoDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OfficialCongestionDemographicDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 특정 혼잡도 ID에 연결된 여러 인구통계 데이터를 일괄 저장
     *
     * @param officialCongestionId official_congestions 테이블의 PK
     * @param demographics         저장할 인구통계 정보 리스트
     */
    public void saveAll(
        Long officialCongestionId,
        List<OfficialCongestionDemographicInfoDto> demographics
    ) {
        if (demographics == null || demographics.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO official_congestion_demographics (
                official_congestion_id, category, sub_category, rate
            ) VALUES (?, ?, ?, ?)
            """;

        List<Object[]> batchArgs = demographics.stream()
            .map(info -> new Object[]{
                officialCongestionId,
                info.category(),
                info.subCategory(),
                info.rate()
            })
            .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}
