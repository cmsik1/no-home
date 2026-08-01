package com.ssafy.home.house.persistence;

import com.ssafy.home.house.dto.HouseSearchCondition;

import java.util.LinkedHashMap;
import java.util.Map;

final class HouseSearchNativeSql {

    private HouseSearchNativeSql() {
    }

    static SearchSql from(HouseSearchCondition condition, boolean orderAndPage) {
        StringBuilder sql = new StringBuilder("""
                FROM house_deals hd
                INNER JOIN houses h ON h.house_id = hd.house_id
                INNER JOIN regions r ON r.region_id = h.region_id
                WHERE hd.house_type = 'apartment'
                """);
        Map<String, Object> params = new LinkedHashMap<>();
        switch (condition.dealMode()) {
            case "jeonse" -> sql.append(" AND hd.deal_type = 'jeonse'");
            case "monthly" -> sql.append(" AND hd.deal_type = 'monthly'");
            case "rent" -> sql.append(" AND hd.deal_type IN ('jeonse', 'monthly')");
            case "all" -> sql.append(" AND hd.deal_type IN ('sale', 'jeonse', 'monthly')");
            default -> sql.append(" AND hd.deal_type = 'sale'");
        }
        append(sql, params, "lawdCd", condition.lawdCd(), " AND hd.lawd_cd = :lawdCd");
        append(sql, params, "sido", condition.sido(), " AND r.sido = :sido");
        append(sql, params, "sigungu", condition.sigungu(), " AND r.sigungu = :sigungu");
        append(sql, params, "umdNm", condition.umdNm(), " AND h.umd_nm = :umdNm");
        append(sql, params, "aptName", condition.aptName(), " AND h.apt_nm LIKE CONCAT('%', :aptName, '%')");
        append(sql, params, "dealYmd", condition.dealYmd(), " AND hd.deal_ymd = :dealYmd");
        if (condition.dealYmd() == null) {
            append(sql, params, "startDealYmd", condition.startDealYmd(), " AND hd.deal_ymd >= :startDealYmd");
            append(sql, params, "endDealYmd", condition.endDealYmd(), " AND hd.deal_ymd <= :endDealYmd");
        }
        append(sql, params, "minPrice", condition.minPrice(), " AND hd.deal_amount_manwon >= :minPrice");
        append(sql, params, "maxPrice", condition.maxPrice(), " AND hd.deal_amount_manwon <= :maxPrice");
        append(sql, params, "minDeposit", condition.minDeposit(), " AND hd.deposit_manwon >= :minDeposit");
        append(sql, params, "maxDeposit", condition.maxDeposit(), " AND hd.deposit_manwon <= :maxDeposit");
        append(sql, params, "minMonthlyRent", condition.minMonthlyRent(), " AND hd.monthly_rent_manwon >= :minMonthlyRent");
        append(sql, params, "maxMonthlyRent", condition.maxMonthlyRent(), " AND hd.monthly_rent_manwon <= :maxMonthlyRent");
        if (orderAndPage) {
            sql.append(orderBy(condition.sort())).append(" LIMIT :size OFFSET :offset");
        }
        return new SearchSql(sql.toString(), params);
    }

    private static void append(StringBuilder sql, Map<String, Object> params, String key, Object value, String clause) {
        if (value != null) {
            sql.append(clause);
            params.put(key, value);
        }
    }

    private static String orderBy(String sort) {
        return switch (sort) {
            case "oldest" -> " ORDER BY hd.deal_date ASC, hd.deal_id ASC";
            case "priceDesc" -> " ORDER BY hd.deal_amount_manwon IS NULL, hd.deal_amount_manwon DESC, hd.deal_date DESC, hd.deal_id DESC";
            case "priceAsc" -> " ORDER BY hd.deal_amount_manwon IS NULL, hd.deal_amount_manwon ASC, hd.deal_date DESC, hd.deal_id DESC";
            case "depositDesc" -> " ORDER BY hd.deposit_manwon IS NULL, hd.deposit_manwon DESC, hd.deal_date DESC, hd.deal_id DESC";
            case "depositAsc" -> " ORDER BY hd.deposit_manwon IS NULL, hd.deposit_manwon ASC, hd.deal_date DESC, hd.deal_id DESC";
            case "monthlyRentDesc" -> " ORDER BY hd.monthly_rent_manwon IS NULL, hd.monthly_rent_manwon DESC, hd.deal_date DESC, hd.deal_id DESC";
            case "monthlyRentAsc" -> " ORDER BY hd.monthly_rent_manwon IS NULL, hd.monthly_rent_manwon ASC, hd.deal_date DESC, hd.deal_id DESC";
            case "areaDesc" -> " ORDER BY hd.exclu_use_ar IS NULL, hd.exclu_use_ar DESC, hd.deal_date DESC, hd.deal_id DESC";
            case "areaAsc" -> " ORDER BY hd.exclu_use_ar IS NULL, hd.exclu_use_ar ASC, hd.deal_date DESC, hd.deal_id DESC";
            default -> " ORDER BY hd.deal_date DESC, hd.deal_id DESC";
        };
    }

    record SearchSql(String sql, Map<String, Object> params) {
    }
}
