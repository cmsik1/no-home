package com.ssafy.home.publicdata.controller;

import com.ssafy.home.common.response.ApiResponse;
import com.ssafy.home.publicdata.dto.PublicDataImportResult;
import com.ssafy.home.publicdata.service.PublicDataImportFacade;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 특정 법정동·거래월의 공공데이터 수집을 수동으로 시작하는 운영용 HTTP 경계다. */
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
