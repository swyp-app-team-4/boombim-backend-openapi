package com.boombim.congestion;

import com.boombim.common.properties.OfficialCongestionPurgeProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfficialCongestionPurgeScheduler {

    private final JdbcTemplate jdbcTemplate;
    private final OfficialCongestionPurgeProperties properties;

    @Scheduled(cron = "0 5 00 * * *", zone = "Asia/Seoul")
    public void purgeTask() {
        if (!properties.enabled()) {
            log.info("[Purge] Disabled. Skip.");
            return;
        }

        LocalDateTime cutoff = LocalDate.now(ZoneId.of("Asia/Seoul"))
            .atStartOfDay()
            .minusDays(properties.days());

        int batch = properties.batchSize();

        log.info("[Purge] Start. cutoff={}, keepDays={}, batchSize={}", cutoff, properties.days(), batch);

        int dem = deleteDemographicsInBatches(cutoff, batch);
        log.info("[Purge] official_congestion_demographics deleted: {}", dem);

        int fc = deleteByObservedAtInBatches("official_congestion_forecasts", "id", cutoff, batch);
        log.info("[Purge] official_congestion_forecasts deleted: {}", fc);

        int co = deleteByObservedAtInBatches("official_congestions", "id", cutoff, batch);
        log.info("[Purge] official_congestions deleted: {}", co);

        log.info("[Purge] Done.");
    }

    private int deleteDemographicsInBatches(LocalDateTime cutoff, int batchSize) {
        String sql = """
            WITH victim AS (
              SELECT d.id
              FROM official_congestion_demographics d
              JOIN official_congestions c ON c.id = d.official_congestion_id
              WHERE c.observed_at < ?
              LIMIT %d
            )
            DELETE x
            FROM official_congestion_demographics x
            JOIN victim v ON x.id = v.id
            """.formatted(batchSize);

        int total = 0;
        while (true) {
            int affected = jdbcTemplate.update(sql, cutoff);
            total += affected;
            if (affected < batchSize) {
                break;
            }
        }
        return total;
    }

    private int deleteByObservedAtInBatches(String table, String idCol, LocalDateTime cutoff, int batchSize) {
        String sql = """
            WITH victim AS (
              SELECT %s
              FROM %s
              WHERE observed_at < ?
              LIMIT %d
            )
            DELETE t
            FROM %s t
            JOIN victim v ON t.%s = v.%s
            """.formatted(idCol, table, batchSize, table, idCol, idCol);

        int total = 0;
        while (true) {
            int affected = jdbcTemplate.update(sql, cutoff);
            total += affected;
            if (affected < batchSize) {
                break;
            }
        }
        return total;
    }
}
