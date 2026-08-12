package com.ssafy.home.publicdata.service;

import com.ssafy.home.publicdata.dto.PublicDataImportResult;
import org.springframework.stereotype.Service;

/** 거래 유형에 따라 매매·전월세 import service를 선택하고 {@code all} 요청의 실행 순서를 조율한다. */
@Service
public class PublicDataImportFacade {

    private final PublicDataImportService tradeImportService;
    private final PublicDataAptRentImportService rentImportService;

    public PublicDataImportFacade(
            PublicDataImportService tradeImportService,
            PublicDataAptRentImportService rentImportService
    ) {
        this.tradeImportService = tradeImportService;
        this.rentImportService = rentImportService;
    }

    public PublicDataImportResult importAptDeals(String lawdCd, String dealYmd, String dealMode) {
        return switch (dealMode) {
            case "sale" -> tradeImportService.importAptTrades(lawdCd, dealYmd);
            case "jeonse", "monthly", "rent" -> rentImportService.importAptRents(lawdCd, dealYmd);
            case "all" -> {
                tradeImportService.importAptTrades(lawdCd, dealYmd);
                yield rentImportService.importAptRents(lawdCd, dealYmd);
            }
            default -> throw new IllegalArgumentException("Unsupported dealMode option: " + dealMode);
        };
    }
}
