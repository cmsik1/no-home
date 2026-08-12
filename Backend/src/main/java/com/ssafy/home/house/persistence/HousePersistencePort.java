package com.ssafy.home.house.persistence;

import com.ssafy.home.house.dto.HouseDealPriceRangeResponse;
import com.ssafy.home.house.dto.HouseDealResponse;
import com.ssafy.home.house.dto.HouseResponse;
import com.ssafy.home.house.dto.HouseSearchCondition;
import com.ssafy.home.house.dto.HouseSearchResultResponse;
import com.ssafy.home.house.dto.ImportBatchResponse;
import com.ssafy.home.house.dto.RegionResponse;

import java.util.List;
import java.util.Optional;

/**
 * 주택 검색 application service가 요구하는 영속성 계약이다.
 * service는 이 인터페이스에만 의존하고 native SQL과 JPA row 변환은 adapter 내부에 남긴다.
 */
public interface HousePersistencePort {
    List<RegionResponse> selectRegionsByLawdCd(String lawdCd);
    List<HouseResponse> selectHousesByAptName(String aptName);
    List<HouseDealResponse> selectHouseDeals(String lawdCd, String dealYmd);
    List<HouseSearchResultResponse> searchHouseDeals(HouseSearchCondition condition);
    long countHouseDeals(HouseSearchCondition condition);
    HouseDealPriceRangeResponse selectHouseDealPriceRange(HouseSearchCondition condition);
    Optional<ImportBatchResponse> selectImportBatch(String sourceApi, String lawdCd, String dealYmd, String houseType, String dealType);
}
