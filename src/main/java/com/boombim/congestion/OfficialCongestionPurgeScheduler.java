package com.boombim.congestion;

import com.boombim.common.properties.OfficialCongestionPurgeProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Scheduled(cron = "0 54 15 * * *", zone = "Asia/Seoul")
    public void purgeTask() {
        if (!properties.enabled()) {
            log.info("[Purge] Disabled. Skip.");
            return;
        }

        // 서버 로컬 타임존 기준(프로덕션 KST 가정)
        LocalDateTime cutoff = LocalDate.now().atStartOfDay().minusDays(properties.days());
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

    // 부모 observed_at 기준으로 자식 먼저 제거
    private int deleteDemographicsInBatches(LocalDateTime cutoff, int batchSize) {
        String sql = """
            WITH victim AS (
              SELECT d.id
              FROM official_congestion_demographics d
              JOIN official_congestions c ON c.id = d.official_congestion_id
              WHERE c.observed_at < ?
              ORDER BY d.id
              LIMIT %d
            )
            DELETE FROM official_congestion_demographics x
            USING victim v
            WHERE x.id = v.id
            """.formatted(batchSize);

        int total = 0;
        while (true) {
            int affected = jdbcTemplate.update(sql, cutoff);
            total += affected;
            if (affected < batchSize)
                break;
        }
        return total;
    }

    // observed_at 기준 일반 테이블 삭제 (배치 분할)
    private int deleteByObservedAtInBatches(String table, String idCol, LocalDateTime cutoff, int batchSize) {
        String sql = """
            WITH victim AS (
              SELECT %s
              FROM %s
              WHERE observed_at < ?
              ORDER BY %s
              LIMIT %d
            )
            DELETE FROM %s t
            USING victim v
            WHERE t.%s = v.%s
            """.formatted(idCol, table, idCol, batchSize, table, idCol, idCol);

        int total = 0;
        while (true) {
            int affected = jdbcTemplate.update(sql, cutoff);
            total += affected;
            if (affected < batchSize)
                break;
        }
        return total;
    }
}
