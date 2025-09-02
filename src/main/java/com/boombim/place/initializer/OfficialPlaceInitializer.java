package com.boombim.place.initializer;

import static com.boombim.common.constant.OfficialPlaceConstant.*;

import com.boombim.openapi.service.OfficialPlaceImageService;
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
    private final OfficialPlaceImageService imageService;

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

            JsonNode ringNode = geometryNode.get(COORDINATES).get(0);

            List<List<Double>> polygonCoordinates = new ArrayList<>();
            List<Coordinate> jtsCoordinates = new ArrayList<>();

            for (JsonNode pair : ringNode) {
                double longitude = pair.get(0).asDouble();
                double latitude = pair.get(1).asDouble();
                polygonCoordinates.add(List.of(longitude, latitude));
                jtsCoordinates.add(new Coordinate(longitude, latitude));
            }

            LinearRing linearRing = geometryFactory
                .createLinearRing(jtsCoordinates.toArray(new Coordinate[0]));
            Polygon polygon = geometryFactory.createPolygon(linearRing, null);

            Point centroid = polygon.getCentroid();
            double centroidLatitude = centroid.getY();
            double centroidLongitude = centroid.getX();

            result.add(OfficialPlaceDto.of(
                name,
                poiCode,
                centroidLatitude,
                centroidLongitude,
                polygonCoordinates
            ));
        }

        officialPlaceDao.initialize(result);
        log.info("{} official places inserted.", result.size());

        imageService.syncAll();
        log.info("Official place images synced.");
    }
}
