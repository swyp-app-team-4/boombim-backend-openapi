package com.boombim.congestion.dto;

import java.time.LocalDateTime;

/**
 * 공식 혼잡도 정보를 담는 DTO 클래스
 *
 * <p>
 * Open API를 통해 받아오는 특정 장소의 혼잡도 데이터를 기반으로<br>
 * 장소명, POI 코드, 혼잡도 수준 및 메시지, 인구수 범위, 성별/연령대별 비율,<br>
 * 거주/비거주 비율, 관측 시각 정보를 포함
 * </p>
 *
 * @param areaName                  장소명 (ex. "강남 MICE 관광특구")
 * @param poiCode                   장소 식별을 위한 POI 코드
 * @param congestionLevel           혼잡도 수준 (ex. "여유", "보통", "약간 붐빔", "붐빔")
 * @param congestionMessage         혼잡도 메시지 (설명 문구)
 * @param populationMin             예상 최소 인구수
 * @param populationMax             예상 최대 인구수
 * @param malePopulationRate        남성 인구 비율 (%)
 * @param femalePopulationRate      여성 인구 비율 (%)
 * @param populationRate0           0~9세 인구 비율 (%)
 * @param populationRate10          10대 인구 비율 (%)
 * @param populationRate20          20대 인구 비율 (%)
 * @param populationRate30          30대 인구 비율 (%)
 * @param populationRate40          40대 인구 비율 (%)
 * @param populationRate50          50대 인구 비율 (%)
 * @param populationRate60          60대 인구 비율 (%)
 * @param populationRate70          70세 이상 인구 비율 (%)
 * @param residentPopulationRate    거주 인구 비율 (%)
 * @param nonResidentPopulationRate 비거주 인구 비율 (%)
 * @param observedAt                데이터 관측 시각
 */
public record OfficialCongestionDto(
    String areaName,
    String poiCode,
    String congestionLevel,
    String congestionMessage,
    Long populationMin,
    Long populationMax,
    Double malePopulationRate,
    Double femalePopulationRate,
    Double populationRate0,
    Double populationRate10,
    Double populationRate20,
    Double populationRate30,
    Double populationRate40,
    Double populationRate50,
    Double populationRate60,
    Double populationRate70,
    Double residentPopulationRate,
    Double nonResidentPopulationRate,
    LocalDateTime observedAt
) {

    public static OfficialCongestionDto of(
        String areaName,
        String poiCode,
        String congestionLevel,
        String congestionMessage,
        Long populationMin,
        Long populationMax,
        Double malePopulationRate,
        Double femalePopulationRate,
        Double populationRate0,
        Double populationRate10,
        Double populationRate20,
        Double populationRate30,
        Double populationRate40,
        Double populationRate50,
        Double populationRate60,
        Double populationRate70,
        Double residentPopulationRate,
        Double nonResidentPopulationRate,
        LocalDateTime observedAt
    ) {
        return new OfficialCongestionDto(
            areaName,
            poiCode,
            congestionLevel,
            congestionMessage,
            populationMin,
            populationMax,
            malePopulationRate,
            femalePopulationRate,
            populationRate0,
            populationRate10,
            populationRate20,
            populationRate30,
            populationRate40,
            populationRate50,
            populationRate60,
            populationRate70,
            residentPopulationRate,
            nonResidentPopulationRate,
            observedAt
        );
    }
}
