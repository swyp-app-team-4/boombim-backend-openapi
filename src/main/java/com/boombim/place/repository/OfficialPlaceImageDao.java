package com.boombim.place.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OfficialPlaceImageDao {

    private final JdbcTemplate jdbc;

    public record BasicRow(Long id, String poiCode, String name, String imageUrl) {

    }

    public List<BasicRow> findAllWithoutImageUrl() {
        String sql = """
                SELECT id, poi_code, name, COALESCE(image_url, '') AS image_url
                FROM official_places
                WHERE image_url IS NULL OR image_url = ''
                ORDER BY id
            """;
        return jdbc.query(sql, this::mapBasic);
    }

    public void updateImageUrlById(Long id, String url) {
        jdbc.update("UPDATE official_places SET image_url = ? WHERE id = ?", url, id);
    }

    private BasicRow mapBasic(ResultSet rs, int i) throws SQLException {
        return new BasicRow(
            rs.getLong("id"),
            rs.getString("poi_code"),
            rs.getString("name"),
            rs.getString("image_url")
        );
    }
}
