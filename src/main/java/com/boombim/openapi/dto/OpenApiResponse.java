package com.boombim.openapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenApiResponse(
    @JsonProperty("SeoulRtd.citydata_ppltn")
    List<CityDataItem> citydataPpltn
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CityDataItem(
        @JsonProperty("AREA_NM") String areaName,
        @JsonProperty("AREA_CD") String areaCode,
        @JsonProperty("AREA_CONGEST_LVL") String congestionLevel,
        @JsonProperty("AREA_CONGEST_MSG") String congestionMessage,
        @JsonProperty("AREA_PPLTN_MIN") Long populationMinimum,
        @JsonProperty("AREA_PPLTN_MAX") Long populationMaximum,
        @JsonProperty("MALE_PPLTN_RATE") Double malePopulationRate,
        @JsonProperty("FEMALE_PPLTN_RATE") Double femalePopulationRate,
        @JsonProperty("PPLTN_RATE_0") Double populationRate0,
        @JsonProperty("PPLTN_RATE_10") Double populationRate10,
        @JsonProperty("PPLTN_RATE_20") Double populationRate20,
        @JsonProperty("PPLTN_RATE_30") Double populationRate30,
        @JsonProperty("PPLTN_RATE_40") Double populationRate40,
        @JsonProperty("PPLTN_RATE_50") Double populationRate50,
        @JsonProperty("PPLTN_RATE_60") Double populationRate60,
        @JsonProperty("PPLTN_RATE_70") Double populationRate70,
        @JsonProperty("RESNT_PPLTN_RATE") Double residentPopulationRate,
        @JsonProperty("NON_RESNT_PPLTN_RATE") Double nonResidentPopulationRate,
        @JsonProperty("PPLTN_TIME")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime populationTime,
        @JsonProperty("FCST_PPLTN") List<ForecastItem> forecast
    ) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ForecastItem(
            @JsonProperty("FCST_TIME")
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime forecastTime,
            @JsonProperty("FCST_CONGEST_LVL") String forecastCongestLevel,
            @JsonProperty("FCST_PPLTN_MIN") Long forecastPopulationMinimum,
            @JsonProperty("FCST_PPLTN_MAX") Long forecastPopulationMaximum
        ) {

        }

    }

}
