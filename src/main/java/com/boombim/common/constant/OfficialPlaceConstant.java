package com.boombim.common.constant;

public final class OfficialPlaceConstant {

    private OfficialPlaceConstant() {
        throw new UnsupportedOperationException("상수 클래스는 인스턴스화 할 수 없습니다.");
    }

    // directory
    public static final String METADATA = "data/official-place-metadata.json";

    // json node
    public static final String FEATURES = "features";
    public static final String PROPERTIES = "properties";
    public static final String GEOMETRY = "geometry";


    public static final String AREA_NM = "AREA_NM";
    public static final String AREA_CD = "AREA_CD";
    public static final String CATEGORY = "CATEGORY";
    public static final String COORDINATES = "coordinates";

    public static final int FIRST_INDEX = 0;
    public static final int SECOND_INDEX = 1;

    // jts
    public static final int LNG = 0;
    public static final int LAT = 1;

}
