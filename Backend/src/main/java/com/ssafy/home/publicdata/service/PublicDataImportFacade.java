package com.ssafy.home.publicdata.service;

import com.ssafy.home.publicdata.dto.PublicDataImportResult;
import org.springframework.stereotype.Service;

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
