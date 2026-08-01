package com.ssafy.home.publicdata.persistence;

public record RegionIdMapping(
        Long regionId,
        String lawdCd,
        String umdNm
) {
}
