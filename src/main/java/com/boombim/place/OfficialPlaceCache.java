package com.boombim.place;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfficialPlaceCache {

    private final JdbcTemplate jdbcTemplate;
    private final Map<String, Long> poiCodeToId = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        jdbcTemplate.query(
            "SELECT poi_code, id FROM official_places",
            rs -> {
                poiCodeToId.put(rs.getString(1), rs.getLong(2));
            }
        );

        log.info("Caching Finished");
        log.info("poiCodeToId Size: {}", poiCodeToId.size());
    }

    public Optional<Long> getOfficialPlaceId(
        String poiCode
    ) {
        Long cachedId = poiCodeToId.get(poiCode);

        if (cachedId != null) {
            return Optional.of(cachedId);
        }

        List<Long> list = jdbcTemplate.query(
            "SELECT id FROM official_places WHERE poi_code = ?",
            (rs, i) -> rs.getLong(1),
            poiCode
        );

        if (list.isEmpty()) {
            return Optional.empty();
        }

        Long id = list.get(0);
        poiCodeToId.put(poiCode, id);

        return Optional.of(id);
    }

    public List<String> getAllPoiCodes() {
        return List.copyOf(poiCodeToId.keySet());
    }

}
