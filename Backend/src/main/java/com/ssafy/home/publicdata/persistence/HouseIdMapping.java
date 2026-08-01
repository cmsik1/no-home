package com.ssafy.home.publicdata.persistence;

public record HouseIdMapping(
        Long houseId,
        String sggCd,
        String umdNm,
        String jibun,
        String aptNm,
        Integer buildYear
) {
}
