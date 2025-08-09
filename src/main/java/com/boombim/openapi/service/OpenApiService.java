package com.boombim.openapi.service;

import com.boombim.congestion.repository.OfficialCongestionDao;
import com.boombim.congestion.repository.OfficialCongestionForecastDao;
import com.boombim.openapi.OpenApiClient;
import com.boombim.openapi.dto.OpenApiResponse;
import com.boombim.place.repository.OfficialPlaceDao;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiService {

    private final OpenApiClient openApiClient;
    private final OfficialPlaceDao officialPlaceDao;
    private final OfficialCongestionDao officialCongestionDao;
    private final OfficialCongestionForecastDao officialCongestionForecastDao;

    public void fetchAndSave() {
        List<String> poiCodes = officialPlaceDao.findAllPoiCodes();

        for (String poiCode : poiCodes) {
            OpenApiResponse response = openApiClient.fetch(poiCode);

            officialCongestionDao.saveAll(response.citydataPpltn());

            response.citydataPpltn().forEach(item -> {
                if (item.forecast() != null && !item.forecast().isEmpty()) {
                    officialCongestionForecastDao.saveAll(
                        item.forecast(),
                        item.populationTime(),
                        item.areaCode()
                    );
                }
            });
        }
    }

}
