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

    public void initialize(
        List<OfficialPlaceDto> officialPlaces
    ) {

        String sql = """
                INSERT INTO official_places (name, poi_code, centroid_latitude, centroid_longitude, polygon_coordinates)
                VALUES (?, ?, ?, ?, ?::jsonb)
                ON CONFLICT (poi_code) DO NOTHING
            """;

        List<Object[]> batchArgs = officialPlaces.stream()
            .map(officialPlace -> {
                try {
                    String json = objectMapper
                        .writeValueAsString(officialPlace.polygonCoordinates());

                    return new Object[]{
                        officialPlace.name(),
                        officialPlace.poiCode(),
                        officialPlace.centroidLatitude(),
                        officialPlace.centroidLongitude(),
                        json
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
