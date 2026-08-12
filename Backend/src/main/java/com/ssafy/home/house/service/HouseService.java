package com.ssafy.home.house.service;

import com.ssafy.home.common.region.SeoulLawdCodeResolver;
import com.ssafy.home.common.region.SeoulLegalDongCatalog;
import com.ssafy.home.common.text.MojibakeRepairer;
import com.ssafy.home.house.dto.HouseDealPriceRangeResponse;
import com.ssafy.home.house.dto.HouseDealResponse;
import com.ssafy.home.house.dto.HouseResponse;
import com.ssafy.home.house.dto.HouseSearchCondition;
import com.ssafy.home.house.dto.HouseSearchPageResponse;
import com.ssafy.home.house.dto.HouseSearchResultResponse;
import com.ssafy.home.house.dto.HouseSearchRequest;
import com.ssafy.home.house.dto.ImportBatchResponse;
import com.ssafy.home.house.dto.RegionResponse;
import com.ssafy.home.house.persistence.HousePersistencePort;
import com.ssafy.home.publicdata.service.PublicDataLiveSearchService;
import com.ssafy.home.publicdata.service.PublicDataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 주택 검색 유스케이스를 조율하는 application service다.
 * HTTP 요청을 검증된 검색 조건으로 바꾸고, data coverage에 따라 실시간 공공데이터 또는 DB 조회를 선택한 뒤
 * page·가격 범위·import metadata가 포함된 응답을 만든다.
 */
@Service
public class HouseService {

    private static final Collator KOREAN_COLLATOR = Collator.getInstance(Locale.KOREAN);

    private final HousePersistencePort housePersistencePort;
    private final PublicDataLiveSearchService publicDataLiveSearchService;
    private final SeoulLawdCodeResolver seoulLawdCodeResolver;
    private final HouseAutoImportCoverage autoImportCoverage;

    @Autowired
    public HouseService(
            HousePersistencePort housePersistencePort,
            PublicDataLiveSearchService publicDataLiveSearchService,
            SeoulLawdCodeResolver seoulLawdCodeResolver,
            HouseAutoImportCoverage autoImportCoverage
    ) {
        this.housePersistencePort = housePersistencePort;
        this.publicDataLiveSearchService = publicDataLiveSearchService;
        this.seoulLawdCodeResolver = seoulLawdCodeResolver;
        this.autoImportCoverage = autoImportCoverage;
    }

    HouseService(HousePersistencePort housePersistencePort) {
        this(housePersistencePort, null, new SeoulLawdCodeResolver(),
                new HouseAutoImportCoverage(housePersistencePort, null, null, null, new SeoulLawdCodeResolver()));
    }

    HouseService(
            HousePersistencePort housePersistencePort,
            PublicDataImportService publicDataImportService,
            SeoulLawdCodeResolver seoulLawdCodeResolver
    ) {
        this(housePersistencePort, null, seoulLawdCodeResolver,
                new HouseAutoImportCoverage(housePersistencePort, publicDataImportService, null, null,
                        seoulLawdCodeResolver));
    }

    public List<RegionResponse> findRegions(String lawdCd) {
        Map<String, RegionResponse> regionsByDong = new LinkedHashMap<>();

        for (RegionResponse region : housePersistencePort.selectRegionsByLawdCd(lawdCd)) {
            RegionResponse repaired = repairRegion(region);
            if (hasText(repaired.umdNm())) {
                regionsByDong.putIfAbsent(repaired.umdNm(), repaired);
            }
        }

        for (RegionResponse region : SeoulLegalDongCatalog.regions(lawdCd, seoulLawdCodeResolver)) {
            regionsByDong.putIfAbsent(region.umdNm(), region);
        }

        return regionsByDong.values().stream()
                .sorted(Comparator.comparing(RegionResponse::umdNm, KOREAN_COLLATOR))
                .toList();
    }

    public List<HouseResponse> findHouses(String aptName) {
        return housePersistencePort.selectHousesByAptName(aptName);
    }

    public List<HouseDealResponse> findHouseDeals(String lawdCd, String dealYmd) {
        return housePersistencePort.selectHouseDeals(lawdCd, dealYmd);
    }

    /**
     * Controller에서 binding한 검색 요청을 최종 실행 메서드에 전달한다. 최종 실행 경로는 coverage가 부족하면
     * 실시간 공공데이터를, 이미 적재된 범위면 persistence port를 사용하고 두 결과를 같은 응답 DTO로 합친다.
     */
    public HouseSearchPageResponse searchHouseDeals(HouseSearchRequest request) {
        return searchHouseDeals(
                request.lawdCd(), request.sido(), request.sigungu(), request.umdNm(), request.aptName(),
                request.dealYmd(), request.startDealYmd(), request.endDealYmd(), request.page(), request.size(),
                request.autoImportEnabled(), request.sort(), request.minPrice(), request.maxPrice(),
                request.minDeposit(), request.maxDeposit(), request.minMonthlyRent(), request.maxMonthlyRent(),
                request.dealMode()
        );
    }

    public HouseDealPriceRangeResponse findHouseDealPriceRange(HouseSearchRequest request) {
        return findHouseDealPriceRange(
                request.lawdCd(), request.sido(), request.sigungu(), request.umdNm(), request.aptName(),
                request.dealYmd(), request.startDealYmd(), request.endDealYmd(), request.autoImportEnabled(),
                request.dealMode()
        );
    }

    public HouseSearchPageResponse searchHouseDeals(
            String lawdCd,
            String sido,
            String sigungu,
            String umdNm,
            String aptName,
            String dealYmd,
            Integer page,
            Integer size
    ) {
        return searchHouseDeals(lawdCd, sido, sigungu, umdNm, aptName, dealYmd, page, size, true);
    }

    public HouseSearchPageResponse searchHouseDeals(
            String lawdCd,
            String sido,
            String sigungu,
            String umdNm,
            String aptName,
            String dealYmd,
            Integer page,
            Integer size,
            Boolean autoImport
    ) {
        return searchHouseDeals(lawdCd, sido, sigungu, umdNm, aptName, dealYmd, null, null, page, size, autoImport,
                HouseSearchConditionFactory.DEFAULT_SORT, null, null, null, null, null, null,
                HouseSearchConditionFactory.DEFAULT_DEAL_MODE);
    }

    public HouseSearchPageResponse searchHouseDeals(
            String lawdCd,
            String sido,
            String sigungu,
            String umdNm,
            String aptName,
            String dealYmd,
            String startDealYmd,
            String endDealYmd,
            Integer page,
            Integer size,
            Boolean autoImport,
            String sort,
            Integer minPrice,
            Integer maxPrice
    ) {
        return searchHouseDeals(lawdCd, sido, sigungu, umdNm, aptName, dealYmd, startDealYmd, endDealYmd, page, size,
                autoImport, sort, minPrice, maxPrice, null, null, null, null,
                HouseSearchConditionFactory.DEFAULT_DEAL_MODE);
    }

    public HouseSearchPageResponse searchHouseDeals(
            String lawdCd,
            String sido,
            String sigungu,
            String umdNm,
            String aptName,
            String dealYmd,
            String startDealYmd,
            String endDealYmd,
            Integer page,
            Integer size,
            Boolean autoImport,
            String sort,
            Integer minPrice,
            Integer maxPrice,
            Integer minDeposit,
            Integer maxDeposit,
            Integer minMonthlyRent,
            Integer maxMonthlyRent,
            String dealMode
    ) {
        HouseSearchCondition condition = HouseSearchConditionFactory.search(
                lawdCd, sido, sigungu, umdNm, aptName, dealYmd, startDealYmd, endDealYmd, page, size,
                sort, minPrice, maxPrice, minDeposit, maxDeposit, minMonthlyRent, maxMonthlyRent, dealMode
        );

        Optional<HouseAutoImportCoverage.LiveCoverageRequest> liveCoverage = autoImportCoverage.liveCoverageRequest(condition, autoImport);
        if (liveCoverage.isPresent()) {
            HouseAutoImportCoverage.LiveCoverageRequest request = liveCoverage.get();
            return publicDataLiveSearchService.search(request.lawdCds(), request.dealYmds(), condition);
        }

        HouseAutoImportCoverage.AutoImportMetadata autoImportMetadata = publicDataLiveSearchService == null
                ? autoImportCoverage.ensureCoverage(condition, autoImport)
                : HouseAutoImportCoverage.AutoImportMetadata.notAttempted();
        HouseSearchPageResponse pageResponse = searchDb(condition);
        return new HouseSearchPageResponse(
                pageResponse.items(),
                pageResponse.page(),
                pageResponse.size(),
                pageResponse.totalCount(),
                pageResponse.minDealAmountManwon(),
                pageResponse.maxDealAmountManwon(),
                pageResponse.minDepositManwon(),
                pageResponse.maxDepositManwon(),
                pageResponse.minMonthlyRentManwon(),
                pageResponse.maxMonthlyRentManwon(),
                autoImportMetadata.attempted(),
                autoImportMetadata.importedRanges(),
                autoImportMetadata.skippedRanges()
        );
    }

    public HouseDealPriceRangeResponse findHouseDealPriceRange(
            String lawdCd,
            String sido,
            String sigungu,
            String umdNm,
            String aptName,
            String dealYmd,
            String startDealYmd,
            String endDealYmd,
            Boolean autoImport,
            String dealMode
    ) {
        HouseSearchCondition condition = HouseSearchConditionFactory.priceRange(
                lawdCd, sido, sigungu, umdNm, aptName, dealYmd, startDealYmd, endDealYmd, dealMode
        );

        Optional<HouseAutoImportCoverage.LiveCoverageRequest> liveCoverage = autoImportCoverage.liveCoverageRequest(condition, autoImport);
        if (liveCoverage.isPresent()) {
            HouseAutoImportCoverage.LiveCoverageRequest request = liveCoverage.get();
            return publicDataLiveSearchService.priceRange(request.lawdCds(), request.dealYmds(), condition);
        }

        if (publicDataLiveSearchService == null) {
            autoImportCoverage.ensureCoverage(condition, autoImport);
        }

        HouseDealPriceRangeResponse priceRange = housePersistencePort.selectHouseDealPriceRange(condition);
        return priceRange == null ? new HouseDealPriceRangeResponse(null, null, null, null, null, null) : priceRange;
    }

    public HouseDealPriceRangeResponse findHouseDealPriceRange(
            String lawdCd,
            String sido,
            String sigungu,
            String umdNm,
            String aptName,
            String dealYmd,
            String startDealYmd,
            String endDealYmd,
            Boolean autoImport
    ) {
        return findHouseDealPriceRange(lawdCd, sido, sigungu, umdNm, aptName, dealYmd, startDealYmd, endDealYmd,
                autoImport, HouseSearchConditionFactory.DEFAULT_DEAL_MODE);
    }

    public List<String> resolveAutoImportLawdCds(String lawdCd, String sido, String sigungu) {
        return seoulLawdCodeResolver.resolveLawdCds(lawdCd, sido, sigungu);
    }

    public boolean isCompleteCoverage(ImportBatchResponse batch) {
        return autoImportCoverage.isCompleteCoverage(batch);
    }

    private HouseSearchPageResponse searchDb(HouseSearchCondition condition) {
        long totalCount = housePersistencePort.countHouseDeals(condition);
        List<HouseSearchResultResponse> items = totalCount == 0
                ? List.of()
                : housePersistencePort.searchHouseDeals(condition);
        HouseDealPriceRangeResponse priceRange = housePersistencePort.selectHouseDealPriceRange(condition);
        Integer minPrice = priceRange == null ? null : priceRange.minDealAmountManwon();
        Integer maxPrice = priceRange == null ? null : priceRange.maxDealAmountManwon();
        Integer minDeposit = priceRange == null ? null : priceRange.minDepositManwon();
        Integer maxDeposit = priceRange == null ? null : priceRange.maxDepositManwon();
        Integer minMonthlyRent = priceRange == null ? null : priceRange.minMonthlyRentManwon();
        Integer maxMonthlyRent = priceRange == null ? null : priceRange.maxMonthlyRentManwon();
        return new HouseSearchPageResponse(items, condition.page(), condition.size(), totalCount, minPrice, maxPrice,
                minDeposit, maxDeposit, minMonthlyRent, maxMonthlyRent, false, List.of(), List.of());
    }

    public Optional<ImportBatchResponse> findImportBatch(
            String sourceApi,
            String lawdCd,
            String dealYmd,
            String houseType,
            String dealType
    ) {
        return autoImportCoverage.findImportBatch(sourceApi, lawdCd, dealYmd, houseType, dealType);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static RegionResponse repairRegion(RegionResponse region) {
        return new RegionResponse(
                region.regionId(),
                region.lawdCd(),
                region.legalDongCode(),
                MojibakeRepairer.repair(region.sido()),
                MojibakeRepairer.repair(region.sigungu()),
                MojibakeRepairer.repair(region.umdNm()),
                region.lat(),
                region.lng()
        );
    }

}
