package com.ssafy.home.house.controller;

import com.ssafy.home.common.response.ApiResponse;
import com.ssafy.home.house.dto.HouseDealPriceRangeResponse;
import com.ssafy.home.house.dto.HouseDealResponse;
import com.ssafy.home.house.dto.HouseResponse;
import com.ssafy.home.house.dto.HouseSearchPageResponse;
import com.ssafy.home.house.dto.HouseSearchRequest;
import com.ssafy.home.house.dto.RegionResponse;
import com.ssafy.home.house.service.HouseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HouseController {

    private final HouseService houseService;

    public HouseController(HouseService houseService) {
        this.houseService = houseService;
    }

    @GetMapping("/regions")
    public ApiResponse<List<RegionResponse>> regions(@RequestParam String lawdCd) {
        return ApiResponse.ok(houseService.findRegions(lawdCd));
    }

    @GetMapping("/houses")
    public ApiResponse<List<HouseResponse>> houses(@RequestParam String aptName) {
        return ApiResponse.ok(houseService.findHouses(aptName));
    }

    @GetMapping("/houses/search")
    public ResponseEntity<ApiResponse<HouseSearchPageResponse>> searchHouses(
            @ModelAttribute HouseSearchRequest request
    ) {
        HouseSearchPageResponse result = houseService.searchHouseDeals(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/houses/price-range")
    public ResponseEntity<ApiResponse<HouseDealPriceRangeResponse>> housePriceRange(
            @ModelAttribute HouseSearchRequest request
    ) {
        HouseDealPriceRangeResponse result = houseService.findHouseDealPriceRange(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/house-deals")
    public ApiResponse<List<HouseDealResponse>> houseDeals(
            @RequestParam String lawdCd,
            @RequestParam String dealYmd
    ) {
        return ApiResponse.ok(houseService.findHouseDeals(lawdCd, dealYmd));
    }
}
