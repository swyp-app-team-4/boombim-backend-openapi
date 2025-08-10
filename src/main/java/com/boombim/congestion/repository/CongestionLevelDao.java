package com.boombim.congestion.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CongestionLevelDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 혼잡도 이름(name)으로 해당 레벨의 고유 ID 조회
     *
     * @param name 혼잡도 수준 이름 (ex. "붐빔")
     * @return Optional<Integer> 혼잡도 수준 ID. 데이터가 없으면 Optional.empty() 반환
     */
    public Optional<Integer> findIdByName(String name) {
        String sql = "SELECT id FROM congestion_levels WHERE name = ?";
        try {
            Integer id = jdbcTemplate.queryForObject(sql, Integer.class, name);
            return Optional.ofNullable(id);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
