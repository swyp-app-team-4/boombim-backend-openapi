package com.boombim.place.initializer;

import static com.boombim.common.constant.OfficialPlaceConstant.*;

import com.boombim.place.dto.OfficialPlaceDto;
import com.boombim.place.repository.OfficialPlaceDao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.locationtech.jts.geom.Coordinate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfficialPlaceInitializer implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory;
    private final OfficialPlaceDao officialPlaceDao;

    @Override
    public void run(String... args) throws Exception {

        InputStream inputStream = new ClassPathResource(METADATA).getInputStream();
        JsonNode root = objectMapper.readTree(inputStream);

        List<OfficialPlaceDto> result = new ArrayList<>();

        for (JsonNode feature : root.get(FEATURES)) {
            JsonNode propertiesNode = feature.get(PROPERTIES);
            JsonNode geometryNode = feature.get(GEOMETRY);

            String name = propertiesNode.get(AREA_NM).asText();
            String poiCode = propertiesNode.get(AREA_CD).asText();
//            String category = propertiesNode.get(CATEGORY).asText();

            // GeoJson Polygon
            JsonNode ringNode = geometryNode.get(COORDINATES).get(FIRST_INDEX);

            // 앱단에 내려줄 좌표 - jsonb로 저장
            List<List<Double>> polygonCoordinates = new ArrayList<>();

            // JTS
            List<Coordinate> jtsCoordinates = new ArrayList<>();

            for (JsonNode pair : ringNode) {
                double longitude = pair.get(LNG).asDouble();
                double latitude = pair.get(LAT).asDouble();
                polygonCoordinates.add(List.of(longitude, latitude));
                jtsCoordinates.add(new Coordinate(longitude, latitude));
            }

            double centroidLatitude;  // 중심 위도
            double centroidLongitude; // 중심 경도

            int size = jtsCoordinates.size();

            LinearRing linearRing = geometryFactory
                .createLinearRing(jtsCoordinates.toArray(new Coordinate[size]));

            Polygon polygon = geometryFactory.createPolygon(linearRing, null);

            Point centroid = polygon.getCentroid();
            centroidLatitude = centroid.getY();
            centroidLongitude = centroid.getX();

            result.add(OfficialPlaceDto.of(
                name,
                poiCode,
                centroidLatitude,
                centroidLongitude,
                polygonCoordinates
            ));
        }

        officialPlaceDao.saveAll(result);
        log.info("{} official places inserted.", result.size());
    }

}
