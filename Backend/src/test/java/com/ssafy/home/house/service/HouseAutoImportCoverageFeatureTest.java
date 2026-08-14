package com.ssafy.home.house.service;

import com.ssafy.home.common.feature.DeploymentFeaturePolicy;
import com.ssafy.home.common.region.SeoulLawdCodeResolver;
import com.ssafy.home.house.dto.HouseSearchCondition;
import com.ssafy.home.house.persistence.HousePersistencePort;
import com.ssafy.home.publicdata.service.PublicDataAptRentImportService;
import com.ssafy.home.publicdata.service.PublicDataImportService;
import com.ssafy.home.publicdata.service.PublicDataLiveSearchService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class HouseAutoImportCoverageFeatureTest {

    @Test
    void disabledLiveSearchReturnsDbOnlyPathWithoutExternalCalls() {
        HousePersistencePort persistence = mock(HousePersistencePort.class);
        PublicDataImportService tradeImport = mock(PublicDataImportService.class);
        PublicDataAptRentImportService rentImport = mock(PublicDataAptRentImportService.class);
        PublicDataLiveSearchService liveSearch = mock(PublicDataLiveSearchService.class);
        HouseAutoImportCoverage coverage = new HouseAutoImportCoverage(
                persistence, tradeImport, rentImport, liveSearch, new SeoulLawdCodeResolver(),
                new DeploymentFeaturePolicy(true, true, false, true));
        HouseSearchCondition condition = new HouseSearchCondition(
                "sale", "11590", "서울특별시", "동작구", null, null, "202605",
                null, null, "latest", null, null, null, null, null, null, 1, 20, 0);

        assertThat(coverage.liveCoverageRequest(condition, true)).isEmpty();
        assertThat(coverage.ensureCoverage(condition, true).attempted()).isFalse();
        verifyNoInteractions(persistence, tradeImport, rentImport, liveSearch);
    }
}
