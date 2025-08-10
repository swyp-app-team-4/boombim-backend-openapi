package com.boombim.openapi;

import static com.boombim.common.constant.OpenApiConstant.*;

import com.boombim.common.properties.OpenApiProperties;
import com.boombim.openapi.dto.OpenApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenApiClient {

    private final WebClient webClient;
    private final OpenApiProperties openApiProperties;

    public OpenApiResponse fetch(
        String poiCode
    ) {

        String apiKey = openApiProperties.apiKey();

        final String endpoint = apiKey + PATH + poiCode;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                OpenApiResponse response = webClient.get()
                    .uri(endpoint)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchangeToMono(clientResponse -> {
                        MediaType contentType = clientResponse.headers()
                            .contentType()
                            .orElse(MediaType.APPLICATION_OCTET_STREAM);

                        if (!contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                            log.warn("[OpenApiClient] 비정상 응답 감지 {} Content-Type: {}", poiCode, contentType);
                            return Mono.empty();
                        }

                        return clientResponse.bodyToMono(OpenApiResponse.class);
                    })
                    .onErrorResume(ex -> {
                        log.warn("[OpenApiClient] 요청 실패 {} - {}", poiCode, ex.toString());
                        return Mono.empty();
                    })
                    .block();

                if (response != null) {
                    return response;
                }

                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("[OpenApiClient] 예외 발생 {} - {}", poiCode, e.toString());
            }
        }

        log.error("[OpenApiClient] 최대 재시도 실패 poiCode={}", poiCode);
        return null;

    }

}
