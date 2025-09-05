package com.boombim.openapi.dto;

import java.util.List;

public record KakaoRegionResponse(
    List<Document> documents
) {

    /**
     * @param region_type        H (행정동) / B (법정동)
     * @param region_2depth_name 구
     * @param region_3depth_name 동
     * @param code               법정동 코드 (10자리)
     */
    public record Document(
        String region_type,
        String region_2depth_name,
        String region_3depth_name,
        String code
    ) {

    }

}
