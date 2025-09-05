package com.boombim.place.dto;

import java.util.List;

/**
 * 공식 장소 정보를 담는 DTO
 *
 * <p>
 * Open API로 수집한 120곳의 장소 데이터를 기반으로 아래 필드를 담습니다.
 * </p>
 *
 * @param name               장소명 (예: "강남 MICE 관광특구")
 * @param poiCode            장소 식별을 위한 POI 코드
 * @param legalDong          법정동 (예: "강남구 삼성동")
 * @param areaM2             윤곽선 다각형 면적(㎡)
 * @param centroidLatitude   중심 위도(다각형 센트로이드)
 * @param centroidLongitude  중심 경도(다각형 센트로이드)
 * @param polygonCoordinates 윤곽선 다각형 좌표(경도,위도) 목록
 */
public record OfficialPlaceDto(
    String name,
    String poiCode,
    String legalDong,
    double areaM2,
    double centroidLatitude,
    double centroidLongitude,
    List<List<Double>> polygonCoordinates
) {

    public static OfficialPlaceDto of(
        String name,
        String poiCode,
        String legalDong,
        double areaM2,
        double centroidLatitude,
        double centroidLongitude,
        List<List<Double>> polygonCoordinates
    ) {
        return new OfficialPlaceDto(
            name,
            poiCode,
            legalDong,
            areaM2,
            centroidLatitude,
            centroidLongitude,
            polygonCoordinates
        );
    }
}
