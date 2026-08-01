package com.ssafy.home.house.dto;

public record HouseSearchRequest(
        String lawdCd,
        String sido,
        String sigungu,
        String umdNm,
        String aptName,
        String dealYmd,
        String startDealYmd,
        String endDealYmd,
        String dealMode,
        Integer page,
        Integer size,
        Boolean autoImport,
        String sort,
        Integer minPrice,
        Integer maxPrice,
        Integer minDeposit,
        Integer maxDeposit,
        Integer minMonthlyRent,
        Integer maxMonthlyRent
) {
    public boolean autoImportEnabled() {
        return autoImport == null || autoImport;
    }
}
