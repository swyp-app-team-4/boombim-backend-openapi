package com.boombim.common.constant;

public final class OpenApiConstant {

    private OpenApiConstant() {
        throw new UnsupportedOperationException("상수 클래스는 인스턴스화 할 수 없습니다.");
    }

    public static final String BASE_URL = "http://openapi.seoul.go.kr:8088/";
    public static final String PATH = "/json/citydata_ppltn/1/5/";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final int MAX_ATTEMPTS = 10;

}
