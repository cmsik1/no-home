package com.ssafy.home.publicdata.controller;

import com.ssafy.home.common.response.ApiResponse;
import com.ssafy.home.publicdata.dto.PublicDataImportResult;
import com.ssafy.home.publicdata.service.PublicDataImportFacade;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public-data")
public class PublicDataImportController {

    private final PublicDataImportFacade importFacade;

    public PublicDataImportController(PublicDataImportFacade importFacade) {
        this.importFacade = importFacade;
    }

    @PostMapping("/apt-trades/import")
    public ApiResponse<PublicDataImportResult> importAptTrades(
            @RequestParam String lawdCd,
            @RequestParam String dealYmd,
            @RequestParam(required = false, defaultValue = "sale") String dealMode
    ) {
        return ApiResponse.ok(importFacade.importAptDeals(lawdCd, dealYmd, dealMode));
    }
}
