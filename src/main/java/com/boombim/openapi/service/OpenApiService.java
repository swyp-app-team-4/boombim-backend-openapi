package com.boombim.openapi.service;

import com.boombim.congestion.dto.OfficialCongestionDemographicInfoDto;
import com.boombim.congestion.repository.CongestionLevelDao;
import com.boombim.congestion.repository.OfficialCongestionDao;
import com.boombim.congestion.repository.OfficialCongestionDemographicDao;
import com.boombim.congestion.repository.OfficialCongestionForecastDao;
import com.boombim.openapi.OpenApiClient;
import com.boombim.openapi.dto.OpenApiResponse;
import com.boombim.openapi.dto.OpenApiResponse.CityDataItem;
import com.boombim.place.repository.OfficialPlaceDao;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiService {

    private final OpenApiClient openApiClient;
    private final OfficialPlaceDao officialPlaceDao;
    private final CongestionLevelDao congestionLevelDao;
    private final OfficialCongestionDao officialCongestionDao;
    private final OfficialCongestionForecastDao officialCongestionForecastDao;
    private final OfficialCongestionDemographicDao officialCongestionDemographicDao;

    /**
     * 모든 장소(POI)에 대한 혼잡도 데이터를 가져와 DB에 저장<br> 해당 메서드 내의 모든 작업은 하나의 트랜잭션으로 처리
     */
    @Transactional
    public void fetchAndSave() {

        // TODO: 서버 최초 실행시 조회해서 메모리에 띄워놓고 관리하는 방식으로 리팩터링
        List<String> poiCodes = officialPlaceDao.findAllPoiCodes();

        for (String poiCode : poiCodes) {
            OpenApiResponse response = openApiClient.fetch(poiCode);
            if (response == null || response.citydataPpltn() == null) {
                log.warn("POI 코드 '{}'에 대한 API 응답이 없습니다.", poiCode);
                continue;
            }

            List<CityDataItem> cityDataItems = response.citydataPpltn();

            for (CityDataItem item : cityDataItems) {

                Integer congestionLevelId = congestionLevelDao.findIdByName(item.congestionLevel())
                    .orElseGet(() -> {
                        log.warn("알 수 없는 혼잡도 레벨 '{}' (poiCode: {}). 이 데이터는 건너뜁니다.", item.congestionLevel(), poiCode);
                        return null;
                    });

                if (congestionLevelId == null) {
                    continue;
                }

                Long officialCongestionId = officialCongestionDao.save(
                    item.areaCode(),
                    congestionLevelId,
                    item.populationMinimum(),
                    item.populationMaximum(),
                    item.populationTime()
                );

                List<OfficialCongestionDemographicInfoDto> demographics = convertToDemographics(item);
                officialCongestionDemographicDao.saveAll(officialCongestionId, demographics);

                saveForecasts(item, poiCode);
            }
        }
    }

    /**
     * 예측 데이터를 저장하는 로직을 별도 메서드로 분리
     */
    private void saveForecasts(CityDataItem item, String poiCode) {
        if (item.forecast() == null || item.forecast().isEmpty()) {
            return;
        }

        List<Object[]> forecastBatchArgs = new ArrayList<>();
        for (OpenApiResponse.CityDataItem.ForecastItem forecastItem : item.forecast()) {

            Integer forecastLevelId = congestionLevelDao.findIdByName(forecastItem.forecastCongestLevel())
                .orElse(null);

            if (forecastLevelId != null) {
                forecastBatchArgs.add(new Object[]{
                    poiCode,
                    item.populationTime(),
                    forecastItem.forecastTime(),
                    forecastLevelId,
                    forecastItem.forecastPopulationMinimum(),
                    forecastItem.forecastPopulationMaximum()
                });
            } else {
                log.warn("알 수 없는 예측 혼잡도 레벨 '{}' (poiCode: {}). 이 예측은 건너뜁니다.", forecastItem.forecastCongestLevel(), poiCode);
            }
        }
        officialCongestionForecastDao.saveAll(forecastBatchArgs);
    }

    /**
     * Open API 응답 아이템을 인구통계 데이터 리스트로 변환하는 메서드
     */
    private List<OfficialCongestionDemographicInfoDto> convertToDemographics(OpenApiResponse.CityDataItem item) {
        List<OfficialCongestionDemographicInfoDto> list = new ArrayList<>();
        list.add(new OfficialCongestionDemographicInfoDto("GENDER", "MALE", item.malePopulationRate()));
        list.add(new OfficialCongestionDemographicInfoDto("GENDER", "FEMALE", item.femalePopulationRate()));
        list.add(new OfficialCongestionDemographicInfoDto("AGE_GROUP", "0s", item.populationRate0()));
        list.add(new OfficialCongestionDemographicInfoDto("AGE_GROUP", "10s", item.populationRate10()));
        list.add(new OfficialCongestionDemographicInfoDto("AGE_GROUP", "20s", item.populationRate20()));
        list.add(new OfficialCongestionDemographicInfoDto("AGE_GROUP", "30s", item.populationRate30()));
        list.add(new OfficialCongestionDemographicInfoDto("AGE_GROUP", "40s", item.populationRate40()));
        list.add(new OfficialCongestionDemographicInfoDto("AGE_GROUP", "50s", item.populationRate50()));
        list.add(new OfficialCongestionDemographicInfoDto("AGE_GROUP", "60s", item.populationRate60()));
        list.add(new OfficialCongestionDemographicInfoDto("AGE_GROUP", "70s", item.populationRate70()));
        list.add(new OfficialCongestionDemographicInfoDto("RESIDENCY", "RESIDENT", item.residentPopulationRate()));
        list.add(new OfficialCongestionDemographicInfoDto("RESIDENCY", "NON_RESIDENT", item.nonResidentPopulationRate()));
        return list;
    }
}
