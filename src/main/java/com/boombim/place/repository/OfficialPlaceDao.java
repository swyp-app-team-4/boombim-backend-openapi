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

        // address, area_m2 포함 + 충돌 시 갱신
        String sql = """
            INSERT INTO official_places
              (name, poi_code, address, centroid_latitude, centroid_longitude, polygon_coordinates, area_m2)
            VALUES (?, ?, ?, ?, ?, ?::jsonb, ?)
            ON CONFLICT (poi_code) DO UPDATE
            SET name                = EXCLUDED.name,
                address             = EXCLUDED.address,
                centroid_latitude   = EXCLUDED.centroid_latitude,
                centroid_longitude  = EXCLUDED.centroid_longitude,
                polygon_coordinates = EXCLUDED.polygon_coordinates,
                area_m2             = EXCLUDED.area_m2
            """;

        List<Object[]> batchArgs = officialPlaces.stream()
            .map(p -> {
                try {
                    String json = objectMapper.writeValueAsString(p.polygonCoordinates());
                    return new Object[]{
                        p.name(),
                        p.poiCode(),
                        p.address(),            // 반드시 non-null (ex. "알수없음")
                        p.centroidLatitude(),
                        p.centroidLongitude(),
                        json,                   // jsonb 캐스팅은 SQL에서
                        p.areaM2()              // NOT NULL 컬럼이면 0보다 큰 값
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
