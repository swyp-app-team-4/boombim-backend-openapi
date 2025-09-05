package com.boombim.place.repository;

import com.boombim.place.dto.OfficialPlaceDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OfficialPlaceDao {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public void initialize(List<OfficialPlaceDto> officialPlaces) {

        // legal_dong, area_m2 포함 + 충돌 시 갱신
        String sql = """
            INSERT INTO official_places
              (name, poi_code, legal_dong, area_m2, centroid_latitude, centroid_longitude, polygon_coordinates)
            VALUES (?, ?, ?, ?, ?, ?, ?::jsonb)
            ON CONFLICT (poi_code) DO UPDATE
            SET name                = EXCLUDED.name,
                legal_dong          = EXCLUDED.legal_dong,
                area_m2             = EXCLUDED.area_m2,
                centroid_latitude   = EXCLUDED.centroid_latitude,
                centroid_longitude  = EXCLUDED.centroid_longitude,
                polygon_coordinates = EXCLUDED.polygon_coordinates
            """;

        List<Object[]> batchArgs = officialPlaces.stream()
            .map(p -> {
                try {
                    String json = objectMapper.writeValueAsString(p.polygonCoordinates());
                    return new Object[]{
                        p.name(),
                        p.poiCode(),
                        p.legalDong(),          // 반드시 non-null (예: "알수없음")
                        p.areaM2(),             // NOT NULL 권장
                        p.centroidLatitude(),
                        p.centroidLongitude(),
                        json                    // jsonb 캐스팅은 SQL에서
                    };
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("좌표 직렬화 실패", e);
                }
            }).toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    public List<String> findAllPoiCodes() {
        return jdbcTemplate.queryForList(
            "SELECT poi_code FROM official_places WHERE poi_code IS NOT NULL",
            String.class
        );
    }
}
