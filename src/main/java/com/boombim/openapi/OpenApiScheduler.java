package com.boombim.openapi;

import com.boombim.openapi.service.OpenApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenApiScheduler {

    private final OpenApiService openApiService;

    @Scheduled(cron = "30 */5 * * * *")
    public void fetch() {
        log.info("[OpenApiScheduler] fetch 시작");
        openApiService.fetchAndSave();
    }

}
