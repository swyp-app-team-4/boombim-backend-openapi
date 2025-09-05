package com.boombim.common.geo;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.geom.*;
import org.locationtech.proj4j.*;

public final class AreaCalculator {

    private static final CRSFactory CRS_FACTORY = new CRSFactory();
    private static final CoordinateTransformFactory CT_FACTORY = new CoordinateTransformFactory();

    private static final CoordinateReferenceSystem WGS84 = CRS_FACTORY.createFromName("EPSG:4326");
    private static final CoordinateReferenceSystem KOREA5179 = CRS_FACTORY.createFromName("EPSG:5179");

    private static final CoordinateTransform TX_TO_5179 = CT_FACTORY.createTransform(WGS84, KOREA5179);
    private static final CoordinateTransform TX_TO_WGS84 = CT_FACTORY.createTransform(KOREA5179, WGS84);

    private static final GeometryFactory GF = new GeometryFactory();

    private AreaCalculator() {
    }

    /**
     * 경위도(lon,lat) 링 좌표 → 면적(m²)
     */
    public static double areaM2FromLonLatRing(List<Coordinate> ringLonLat) {
        if (ringLonLat == null || ringLonLat.size() < 3)
            return 0d;

        Polygon poly5179 = polygon5179FromLonLatRing(ringLonLat);
        return Math.abs(poly5179.getArea()); // m²
    }

    /**
     * 경위도(lon,lat) 링 좌표 → 센트로이드(경위도 반환)
     */
    public static Coordinate centroidLonLatFromLonLatRing(List<Coordinate> ringLonLat) {
        if (ringLonLat == null || ringLonLat.size() < 3)
            return null;

        Polygon poly5179 = polygon5179FromLonLatRing(ringLonLat);
        Point c5179 = poly5179.getCentroid(); // meters(5179)

        // 5179 → WGS84
        ProjCoordinate in = new ProjCoordinate(c5179.getX(), c5179.getY());
        ProjCoordinate out = new ProjCoordinate();
        TX_TO_WGS84.transform(in, out); // out.x=lon, out.y=lat

        return new Coordinate(out.x, out.y); // (lon, lat)
    }

    /**
     * 경위도 링을 5179로 투영해 닫힌 링으로 Polygon 생성
     */
    private static Polygon polygon5179FromLonLatRing(List<Coordinate> ringLonLat) {
        List<Coordinate> projected = new ArrayList<>(ringLonLat.size() + 1);
        ProjCoordinate in = new ProjCoordinate();
        ProjCoordinate out = new ProjCoordinate();

        for (Coordinate c : ringLonLat) {
            in.x = c.x; // lon
            in.y = c.y; // lat
            TX_TO_5179.transform(in, out); // meters
            projected.add(new Coordinate(out.x, out.y));
        }
        // close ring
        if (!projected.get(0).equals2D(projected.get(projected.size() - 1))) {
            projected.add(new Coordinate(projected.get(0)));
        }

        LinearRing shell = GF.createLinearRing(projected.toArray(Coordinate[]::new));
        return GF.createPolygon(shell, null);
    }
}
