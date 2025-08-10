package com.boombim.congestion.dto;

/**
 *
 * 인구 통계 정보를 전달하기 위한 DTO
 *
 * @param category    통계 대분류 (ex. "GENDER", "AGE_GROUP")
 * @param subCategory 통계 소분류 (ex. "MALE", "FEMALE")
 * @param rate        해당 비율 값
 */
public record OfficialCongestionDemographicInfoDto(
    String category,
    String subCategory,
    Double rate
) {

}
