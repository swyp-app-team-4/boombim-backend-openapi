package com.boombim.place.initializer;

import static com.boombim.common.constant.OfficialPlaceConstant.*;

import com.boombim.common.geo.AreaCalculator;
import com.boombim.openapi.dto.LegalDong;
import com.boombim.openapi.service.KakaoRegionCodeService;
import com.boombim.openapi.service.OfficialPlaceImageService;
import com.boombim.place.dto.OfficialPlaceDto;
import com.boombim.place.repository.OfficialPlaceDao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfficialPlaceInitializer implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final OfficialPlaceDao officialPlaceDao;
    private final OfficialPlaceImageService imageService;
    private final KakaoRegionCodeService kakaoRegionCodeService;

    @Override
    public void run(String... args) throws Exception {
        InputStream inputStream = new ClassPathResource(METADATA).getInputStream();
        JsonNode root = objectMapper.readTree(inputStream);

        List<OfficialPlaceDto> batch = new ArrayList<>();

        for (JsonNode feature : root.get(FEATURES)) {
            JsonNode props = feature.get(PROPERTIES);
            JsonNode geom = feature.get(GEOMETRY);

            String name = props.get(AREA_NM).asText();
            String poiCode = props.get(AREA_CD).asText();

            // 외곽 링
            JsonNode ringNode = geom.get(COORDINATES).get(0);

            List<List<Double>> polygonCoordinates = new ArrayList<>();
            List<Coordinate> ringLonLat = new ArrayList<>();

            for (JsonNode pair : ringNode) {
                double lon = pair.get(0).asDouble();
                double lat = pair.get(1).asDouble();
                polygonCoordinates.add(List.of(lon, lat));
                ringLonLat.add(new Coordinate(lon, lat)); // (lon, lat)
            }

            // 면적(m²)
            double areaM2 = AreaCalculator.areaM2FromLonLatRing(ringLonLat);
            if (areaM2 <= 0) {
                log.warn("Invalid polygon area. Skip poiCode={}, name={}", poiCode, name);
                continue;
            }

            // 센트로이드(경위도)
            Coordinate centerLonLat = AreaCalculator.centroidLonLatFromLonLatRing(ringLonLat);
            if (centerLonLat == null) {
                log.warn("Centroid null. Skip poiCode={}, name={}", poiCode, name);
                continue;
            }
            double centroidLon = centerLonLat.x;
            double centroidLat = centerLonLat.y;

            // 주소(법정동) 역지오코딩
            Optional<LegalDong> legal = kakaoRegionCodeService.getLegalDong(centroidLon, centroidLat);
            String address = legal.map(LegalDong::display).orElse("알수없음");

            batch.add(OfficialPlaceDto.of(
                name,
                poiCode,
                address,
                areaM2,
                centroidLat,
                centroidLon,
                polygonCoordinates
            ));
        }

        // upsert
        officialPlaceDao.initialize(batch);
        log.info("{} official places upserted.", batch.size());

        // 이미지 동기화
        imageService.syncAll();
        log.info("Official place images synced.");
    }
}
