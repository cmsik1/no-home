package com.ssafy.home.house.service;

import com.ssafy.home.common.region.SeoulLawdCodeResolver;
import com.ssafy.home.house.dto.AutoImportRangeResponse;
import com.ssafy.home.house.dto.HouseSearchCondition;
import com.ssafy.home.house.dto.ImportBatchResponse;
import com.ssafy.home.house.persistence.HousePersistencePort;
import com.ssafy.home.publicdata.dto.PublicDataImportResult;
import com.ssafy.home.publicdata.service.AptRentImportCommandFactory;
import com.ssafy.home.publicdata.service.PublicDataApiException;
import com.ssafy.home.publicdata.service.PublicDataAptRentImportService;
import com.ssafy.home.publicdata.service.PublicDataImportService;
import com.ssafy.home.publicdata.service.PublicDataLiveSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ssafy.home.publicdata.service.AptTradeImportCommandFactory.DEAL_TYPE;
import static com.ssafy.home.publicdata.service.AptTradeImportCommandFactory.HOUSE_TYPE;
import static com.ssafy.home.publicdata.service.AptTradeImportCommandFactory.SOURCE_API;

/**
 * 검색 범위가 DB에 완전히 적재됐는지 판단하고 부족한 범위를 실시간 조회 또는 import 대상으로 만든다.
 * 검색 service가 batch table과 외부 API의 세부 규칙을 직접 알지 않게 하는 coverage 정책 객체다.
 */
@Component
final class HouseAutoImportCoverage {

    private static final Logger log = LoggerFactory.getLogger(HouseAutoImportCoverage.class);
    private static final DateTimeFormatter DEAL_YMD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final HousePersistencePort housePersistencePort;
    private final PublicDataImportService publicDataImportService;
    private final PublicDataAptRentImportService publicDataAptRentImportService;
    private final PublicDataLiveSearchService publicDataLiveSearchService;
    private final SeoulLawdCodeResolver seoulLawdCodeResolver;

    HouseAutoImportCoverage(
            HousePersistencePort housePersistencePort,
            PublicDataImportService publicDataImportService,
            PublicDataAptRentImportService publicDataAptRentImportService,
            PublicDataLiveSearchService publicDataLiveSearchService,
            SeoulLawdCodeResolver seoulLawdCodeResolver
    ) {
        this.housePersistencePort = housePersistencePort;
        this.publicDataImportService = publicDataImportService;
        this.publicDataAptRentImportService = publicDataAptRentImportService;
        this.publicDataLiveSearchService = publicDataLiveSearchService;
        this.seoulLawdCodeResolver = seoulLawdCodeResolver;
    }

    AutoImportMetadata ensureCoverage(HouseSearchCondition condition, Boolean autoImport) {
        if (!Boolean.TRUE.equals(autoImport) || !canAutoImport(condition)) {
            return AutoImportMetadata.notAttempted();
        }

        List<String> lawdCds = seoulLawdCodeResolver.resolveLawdCds(
                condition.lawdCd(), condition.sido(), condition.sigungu()
        );
        if (lawdCds.isEmpty()) {
            return AutoImportMetadata.notAttempted();
        }

        List<String> dealYmds = autoImportDealYmds(condition);
        if (dealYmds.isEmpty()) {
            return AutoImportMetadata.notAttempted();
        }

        List<AutoImportRangeResponse> importedRanges = new ArrayList<>();
        List<AutoImportRangeResponse> skippedRanges = new ArrayList<>();
        for (String lawdCd : lawdCds) {
            for (String dealYmd : dealYmds) {
                if (shouldImportSale(condition.dealMode())) {
                    importCoverage(lawdCd, dealYmd, SOURCE_API, HOUSE_TYPE, DEAL_TYPE,
                            () -> publicDataImportService.importAptTrades(lawdCd, dealYmd),
                            importedRanges, skippedRanges);
                }
                if (shouldImportRent(condition.dealMode())) {
                    importCoverage(lawdCd, dealYmd,
                            AptRentImportCommandFactory.SOURCE_API,
                            AptRentImportCommandFactory.HOUSE_TYPE,
                            AptRentImportCommandFactory.DEAL_TYPE,
                            () -> publicDataAptRentImportService.importAptRents(lawdCd, dealYmd),
                            importedRanges, skippedRanges);
                }
            }
        }
        return new AutoImportMetadata(!importedRanges.isEmpty() || !skippedRanges.isEmpty(), importedRanges, skippedRanges);
    }

    Optional<LiveCoverageRequest> liveCoverageRequest(HouseSearchCondition condition, Boolean autoImport) {
        if (!Boolean.TRUE.equals(autoImport) || publicDataLiveSearchService == null || !canAutoImport(condition)) {
            return Optional.empty();
        }

        List<String> lawdCds = seoulLawdCodeResolver.resolveLawdCds(
                condition.lawdCd(), condition.sido(), condition.sigungu()
        );
        List<String> dealYmds = autoImportDealYmds(condition);
        if (lawdCds.isEmpty() || dealYmds.isEmpty()) {
            return Optional.empty();
        }

        for (String lawdCd : lawdCds) {
            for (String dealYmd : dealYmds) {
                if (shouldImportSale(condition.dealMode())
                        && !hasCompleteCoverage(lawdCd, dealYmd, SOURCE_API, HOUSE_TYPE, DEAL_TYPE)) {
                    return Optional.of(new LiveCoverageRequest(lawdCds, dealYmds));
                }
                if (shouldImportRent(condition.dealMode())
                        && !hasCompleteCoverage(lawdCd, dealYmd,
                        AptRentImportCommandFactory.SOURCE_API,
                        AptRentImportCommandFactory.HOUSE_TYPE,
                        AptRentImportCommandFactory.DEAL_TYPE)) {
                    return Optional.of(new LiveCoverageRequest(lawdCds, dealYmds));
                }
            }
        }

        return Optional.empty();
    }

    Optional<ImportBatchResponse> findImportBatch(
            String sourceApi,
            String lawdCd,
            String dealYmd,
            String houseType,
            String dealType
    ) {
        return housePersistencePort.selectImportBatch(sourceApi, lawdCd, dealYmd, houseType, dealType);
    }

    boolean isCompleteCoverage(ImportBatchResponse batch) {
        int totalCount = batch.totalCount() == null ? 0 : batch.totalCount();
        int importedCount = batch.importedCount() == null ? 0 : batch.importedCount();
        int skippedCount = batch.skippedCount() == null ? 0 : batch.skippedCount();
        return "success".equals(batch.status()) && totalCount <= importedCount + skippedCount;
    }

    private boolean hasCompleteCoverage(
            String lawdCd,
            String dealYmd,
            String sourceApi,
            String houseType,
            String dealType
    ) {
        return findImportBatch(sourceApi, lawdCd, dealYmd, houseType, dealType)
                .map(this::isCompleteCoverage)
                .orElse(false);
    }

    private static List<String> autoImportDealYmds(HouseSearchCondition condition) {
        if (hasText(condition.dealYmd())) {
            return List.of(condition.dealYmd());
        }
        if (!hasText(condition.startDealYmd()) || !hasText(condition.endDealYmd())) {
            return List.of();
        }

        YearMonth start = YearMonth.parse(condition.startDealYmd(), DEAL_YMD_FORMATTER);
        YearMonth end = YearMonth.parse(condition.endDealYmd(), DEAL_YMD_FORMATTER);
        List<String> dealYmds = new ArrayList<>();
        for (YearMonth current = start; !current.isAfter(end); current = current.plusMonths(1)) {
            dealYmds.add(current.format(DEAL_YMD_FORMATTER));
        }
        return dealYmds;
    }

    private void importCoverage(
            String lawdCd,
            String dealYmd,
            String sourceApi,
            String houseType,
            String dealType,
            ImportAction importAction,
            List<AutoImportRangeResponse> importedRanges,
            List<AutoImportRangeResponse> skippedRanges
    ) {
        Optional<ImportBatchResponse> batch = findImportBatch(sourceApi, lawdCd, dealYmd, houseType, dealType);
        if (batch.isPresent() && isCompleteCoverage(batch.get())) {
            skippedRanges.add(new AutoImportRangeResponse(lawdCd, dealYmd, "success",
                    sourceApi + " complete coverage exists"));
            return;
        }

        try {
            PublicDataImportResult result = importAction.importData();
            importedRanges.add(new AutoImportRangeResponse(lawdCd, dealYmd, result.status(), result.message()));
        } catch (RuntimeException exception) {
            AutoImportException.Reason reason = classifyAutoImportFailure(exception);
            log.warn("Auto import failed: reason={}, sourceApi={}, lawdCd={}, dealYmd={}{}",
                    reason, sourceApi, lawdCd, dealYmd, autoImportFailureDetail(exception));
            throw new AutoImportException(reason,
                    "Auto import failed for lawdCd=" + lawdCd + ", dealYmd=" + dealYmd,
                    exception);
        }
    }

    private boolean canAutoImport(HouseSearchCondition condition) {
        if (!hasText(condition.dealYmd())
                && (!hasText(condition.startDealYmd()) || !hasText(condition.endDealYmd()))) {
            return false;
        }
        if (shouldImportSale(condition.dealMode()) && publicDataImportService == null) {
            return false;
        }
        if (shouldImportRent(condition.dealMode()) && publicDataAptRentImportService == null) {
            return false;
        }
        if (hasText(condition.aptName())
                && !hasText(condition.lawdCd())
                && !hasText(condition.sido())
                && !hasText(condition.sigungu())
                && !hasText(condition.umdNm())) {
            return false;
        }
        return hasText(condition.lawdCd()) || hasText(condition.sido());
    }

    private static boolean shouldImportSale(String dealMode) {
        return "sale".equals(dealMode) || "all".equals(dealMode);
    }

    private static boolean shouldImportRent(String dealMode) {
        return "jeonse".equals(dealMode) || "monthly".equals(dealMode)
                || "rent".equals(dealMode) || "all".equals(dealMode);
    }

    // Exposes safe diagnostics only; exception messages may include request URLs with service keys.
    private static String autoImportFailureDetail(RuntimeException exception) {
        if (exception instanceof PublicDataApiException publicDataApiException) {
            return ", resultCode=" + publicDataApiException.resultCode()
                    + ", resultMsg=" + publicDataApiException.resultMsg();
        }
        return ", cause=" + exception.getClass().getSimpleName();
    }

    private static AutoImportException.Reason classifyAutoImportFailure(RuntimeException exception) {
        if (exception instanceof PublicDataApiException publicDataApiException) {
            return switch (publicDataApiException.reason()) {
                case KEY_INVALID -> AutoImportException.Reason.KEY_INVALID;
                case QUOTA -> AutoImportException.Reason.QUOTA;
                case PROVIDER_ERROR -> AutoImportException.Reason.PROVIDER_ERROR;
            };
        }
        if (exception instanceof IllegalStateException) {
            return AutoImportException.Reason.KEY_MISSING;
        }
        if (exception instanceof ResourceAccessException || hasCause(exception, SocketTimeoutException.class)) {
            return AutoImportException.Reason.TIMEOUT;
        }
        return AutoImportException.Reason.PROVIDER_ERROR;
    }

    private static boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable current = throwable;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    record AutoImportMetadata(
            boolean attempted,
            List<AutoImportRangeResponse> importedRanges,
            List<AutoImportRangeResponse> skippedRanges
    ) {
        static AutoImportMetadata notAttempted() {
            return new AutoImportMetadata(false, List.of(), List.of());
        }
    }

    record LiveCoverageRequest(List<String> lawdCds, List<String> dealYmds) {
    }

    @FunctionalInterface
    private interface ImportAction {
        PublicDataImportResult importData();
    }
}
