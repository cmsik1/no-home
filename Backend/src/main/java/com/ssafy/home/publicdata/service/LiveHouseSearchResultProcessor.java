package com.ssafy.home.publicdata.service;

import com.ssafy.home.house.dto.AutoImportRangeResponse;
import com.ssafy.home.house.dto.HouseDealPriceRangeResponse;
import com.ssafy.home.house.dto.HouseSearchCondition;
import com.ssafy.home.house.dto.HouseSearchPageResponse;
import com.ssafy.home.house.dto.HouseSearchResultResponse;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static com.ssafy.home.publicdata.service.AptRentImportCommandFactory.RENT_TYPE_JEONSE;
import static com.ssafy.home.publicdata.service.AptRentImportCommandFactory.RENT_TYPE_MONTHLY;

final class LiveHouseSearchResultProcessor {

    private LiveHouseSearchResultProcessor() {
    }

    static HouseSearchPageResponse process(
            List<HouseSearchResultResponse> rows,
            List<AutoImportRangeResponse> ranges,
            HouseSearchCondition condition
    ) {
        List<HouseSearchResultResponse> baseFiltered = baseFiltered(rows, condition);
        HouseDealPriceRangeResponse priceRange = priceRange(baseFiltered);
        List<HouseSearchResultResponse> filtered = baseFiltered.stream()
                .filter(item -> matchesPriceFilters(item, condition))
                .sorted(comparator(condition.sort()))
                .toList();
        int from = Math.min(condition.offset(), filtered.size());
        int to = Math.min(from + condition.size(), filtered.size());
        return new HouseSearchPageResponse(
                filtered.subList(from, to), condition.page(), condition.size(), filtered.size(),
                priceRange.minDealAmountManwon(), priceRange.maxDealAmountManwon(),
                priceRange.minDepositManwon(), priceRange.maxDepositManwon(),
                priceRange.minMonthlyRentManwon(), priceRange.maxMonthlyRentManwon(),
                true, ranges.stream().distinct().toList(), List.of()
        );
    }

    static HouseDealPriceRangeResponse priceRange(
            List<HouseSearchResultResponse> rows,
            HouseSearchCondition condition
    ) {
        return priceRange(baseFiltered(rows, condition));
    }

    private static List<HouseSearchResultResponse> baseFiltered(
            List<HouseSearchResultResponse> rows,
            HouseSearchCondition condition
    ) {
        return rows.stream().filter(item -> matchesBaseFilters(item, condition)).toList();
    }

    private static boolean matchesBaseFilters(HouseSearchResultResponse item, HouseSearchCondition condition) {
        if (condition.umdNm() != null && !condition.umdNm().equals(item.umdNm())) {
            return false;
        }
        if (condition.aptName() != null && !item.aptNm().toLowerCase(Locale.KOREAN)
                .contains(condition.aptName().toLowerCase(Locale.KOREAN))) {
            return false;
        }
        return switch (condition.dealMode()) {
            case "sale" -> "sale".equals(item.dealType());
            case "jeonse" -> RENT_TYPE_JEONSE.equals(item.dealType());
            case "monthly" -> RENT_TYPE_MONTHLY.equals(item.dealType());
            case "rent" -> RENT_TYPE_JEONSE.equals(item.dealType()) || RENT_TYPE_MONTHLY.equals(item.dealType());
            case "all" -> true;
            default -> false;
        };
    }

    private static boolean matchesPriceFilters(HouseSearchResultResponse item, HouseSearchCondition condition) {
        return atLeast(item.dealAmountManwon(), condition.minPrice())
                && atMost(item.dealAmountManwon(), condition.maxPrice())
                && atLeast(item.depositManwon(), condition.minDeposit())
                && atMost(item.depositManwon(), condition.maxDeposit())
                && atLeast(item.monthlyRentManwon(), condition.minMonthlyRent())
                && atMost(item.monthlyRentManwon(), condition.maxMonthlyRent());
    }

    private static boolean atLeast(Integer value, Integer min) {
        return min == null || (value != null && value >= min);
    }

    private static boolean atMost(Integer value, Integer max) {
        return max == null || (value != null && value <= max);
    }

    private static Comparator<HouseSearchResultResponse> comparator(String sort) {
        Comparator<HouseSearchResultResponse> latest = Comparator
                .comparing(HouseSearchResultResponse::dealDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(item -> item.resultKey() == null ? "" : item.resultKey(), Comparator.reverseOrder());
        return switch (sort) {
            case "oldest" -> Comparator
                    .comparing(HouseSearchResultResponse::dealDate, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(item -> item.resultKey() == null ? "" : item.resultKey());
            case "priceDesc" -> byInteger(HouseSearchResultResponse::dealAmountManwon, false).thenComparing(latest);
            case "priceAsc" -> byInteger(HouseSearchResultResponse::dealAmountManwon, true).thenComparing(latest);
            case "depositDesc" -> byInteger(HouseSearchResultResponse::depositManwon, false).thenComparing(latest);
            case "depositAsc" -> byInteger(HouseSearchResultResponse::depositManwon, true).thenComparing(latest);
            case "monthlyRentDesc" -> byInteger(HouseSearchResultResponse::monthlyRentManwon, false).thenComparing(latest);
            case "monthlyRentAsc" -> byInteger(HouseSearchResultResponse::monthlyRentManwon, true).thenComparing(latest);
            case "areaDesc" -> byBigDecimal(HouseSearchResultResponse::excluUseAr, false).thenComparing(latest);
            case "areaAsc" -> byBigDecimal(HouseSearchResultResponse::excluUseAr, true).thenComparing(latest);
            default -> latest;
        };
    }

    private static Comparator<HouseSearchResultResponse> byInteger(Function<HouseSearchResultResponse, Integer> getter, boolean asc) {
        Comparator<Integer> values = asc ? Comparator.naturalOrder() : Comparator.reverseOrder();
        return Comparator.comparing(getter, Comparator.nullsLast(values));
    }

    private static Comparator<HouseSearchResultResponse> byBigDecimal(Function<HouseSearchResultResponse, BigDecimal> getter, boolean asc) {
        Comparator<BigDecimal> values = asc ? Comparator.naturalOrder() : Comparator.reverseOrder();
        return Comparator.comparing(getter, Comparator.nullsLast(values));
    }

    private static HouseDealPriceRangeResponse priceRange(List<HouseSearchResultResponse> items) {
        return new HouseDealPriceRangeResponse(
                min(items, HouseSearchResultResponse::dealAmountManwon),
                max(items, HouseSearchResultResponse::dealAmountManwon),
                min(items, HouseSearchResultResponse::depositManwon),
                max(items, HouseSearchResultResponse::depositManwon),
                min(items, HouseSearchResultResponse::monthlyRentManwon),
                max(items, HouseSearchResultResponse::monthlyRentManwon)
        );
    }

    private static Integer min(List<HouseSearchResultResponse> items, Function<HouseSearchResultResponse, Integer> getter) {
        return items.stream().map(getter).filter(value -> value != null).min(Integer::compareTo).orElse(null);
    }

    private static Integer max(List<HouseSearchResultResponse> items, Function<HouseSearchResultResponse, Integer> getter) {
        return items.stream().map(getter).filter(value -> value != null).max(Integer::compareTo).orElse(null);
    }
}
