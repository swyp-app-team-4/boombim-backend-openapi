package com.boombim.openapi.service;

import com.boombim.openapi.dto.KakaoRegionResponse;
import com.boombim.openapi.dto.LegalDong;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoRegionCodeService {

    private final WebClient kakaoWebClient;

    public Optional<LegalDong> getLegalDong(double lon, double lat) {
        try {
            var res = kakaoWebClient.get()
                .uri(u -> u.path("/v2/local/geo/coord2regioncode.json")
                    .queryParam("x", lon)
                    .queryParam("y", lat)
                    .build())
                .retrieve()
                .bodyToMono(KakaoRegionResponse.class)
                .block(Duration.ofSeconds(3));

            if (res == null || res.documents() == null)
                return Optional.empty();

            return res.documents().stream()
                .filter(d -> "B".equalsIgnoreCase(d.region_type()))
                .findFirst()
                .map(d -> new LegalDong(d.region_2depth_name(), d.region_3depth_name(), d.code()));

        } catch (Exception e) {
            log.warn("coord2regioncode failed (lon={}, lat={}): {}", lon, lat, e.toString());
            return Optional.empty();
        }

    }
}
