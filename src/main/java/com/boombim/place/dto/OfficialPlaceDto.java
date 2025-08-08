package com.boombim.place.dto;

import java.util.List;

/**
 * 공식 장소 정보를 담는 DTO 클래스
 *
 * <p>
 * Open API를 통해 받아오는 120곳의 장소 데이터를 기반으로
 * 장소명, POI 코드, 중심 위도, 중심 경도, 윤곽선 좌표 목록 포함
 * </p>
 *
 * @param name  장소명 (ex. "강남 MICE 관광특구")
 * @param poiCode   장소 식별을 위한 POI 코드
 * @param latitude  중심의 위도 (윤곽선 다각형의 중심점 기준)
 * @param longitude 중심의 경도 (윤곽선 다각형의 중심점 기준)
 * @param boundaryCoordinates   윤곽선 다각형을 구성하는 좌표 목록
 */
public record OfficialPlaceDto(
    String name,
    String poiCode,
    double latitude,
    double longitude,
    List<List<Double>> boundaryCoordinates
) {

}
