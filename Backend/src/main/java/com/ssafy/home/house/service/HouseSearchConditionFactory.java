package com.ssafy.home.house.service;

import com.ssafy.home.house.dto.HouseSearchCondition;

final class HouseSearchConditionFactory {

    static final int DEFAULT_PAGE = 1;
    static final int DEFAULT_SIZE = 20;
    static final String DEFAULT_SORT = "latest";
    static final String DEFAULT_DEAL_MODE = "sale";

    private static final int MAX_SIZE = 100;

    private HouseSearchConditionFactory() {
    }

    static HouseSearchCondition search(
            String lawdCd,
            String sido,
            String sigungu,
            String umdNm,
            String aptName,
            String dealYmd,
            String startDealYmd,
            String endDealYmd,
            Integer page,
            Integer size,
            String sort,
            Integer minPrice,
            Integer maxPrice,
            Integer minDeposit,
            Integer maxDeposit,
            Integer minMonthlyRent,
            Integer maxMonthlyRent,
            String dealMode
    ) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizeSize(size);
        String normalizedDealMode = normalizeDealMode(dealMode);
        String normalizedSort = normalizeSort(sort, normalizedDealMode);
        Integer normalizedMinPrice = normalizePrice(minPrice, "minPrice");
        Integer normalizedMaxPrice = normalizePrice(maxPrice, "maxPrice");
        Integer normalizedMinDeposit = normalizePrice(minDeposit, "minDeposit");
        Integer normalizedMaxDeposit = normalizePrice(maxDeposit, "maxDeposit");
        Integer normalizedMinMonthlyRent = normalizePrice(minMonthlyRent, "minMonthlyRent");
        Integer normalizedMaxMonthlyRent = normalizePrice(maxMonthlyRent, "maxMonthlyRent");
        validateRange(normalizedMinPrice, normalizedMaxPrice, "minPrice", "maxPrice");
        validateRange(normalizedMinDeposit, normalizedMaxDeposit, "minDeposit", "maxDeposit");
        validateRange(normalizedMinMonthlyRent, normalizedMaxMonthlyRent, "minMonthlyRent", "maxMonthlyRent");
        validatePriceFilters(normalizedDealMode, normalizedMinPrice, normalizedMaxPrice, normalizedMinDeposit,
                normalizedMaxDeposit, normalizedMinMonthlyRent, normalizedMaxMonthlyRent);
        DealYmdRange normalizedDealYmds = normalizeDealYmds(dealYmd, startDealYmd, endDealYmd);

        return validated(new HouseSearchCondition(
                normalizedDealMode,
                trimToNull(lawdCd),
                trimToNull(sido),
                trimToNull(sigungu),
                trimToNull(umdNm),
                trimToNull(aptName),
                normalizedDealYmds.dealYmd(),
                normalizedDealYmds.startDealYmd(),
                normalizedDealYmds.endDealYmd(),
                normalizedSort,
                normalizedMinPrice,
                normalizedMaxPrice,
                normalizedMinDeposit,
                normalizedMaxDeposit,
                normalizedMinMonthlyRent,
                normalizedMaxMonthlyRent,
                normalizedPage,
                normalizedSize,
                (normalizedPage - 1) * normalizedSize
        ));
    }

    static HouseSearchCondition priceRange(
            String lawdCd,
            String sido,
            String sigungu,
            String umdNm,
            String aptName,
            String dealYmd,
            String startDealYmd,
            String endDealYmd,
            String dealMode
    ) {
        String normalizedDealMode = normalizeDealMode(dealMode);
        DealYmdRange normalizedDealYmds = normalizeDealYmds(dealYmd, startDealYmd, endDealYmd);
        return validated(new HouseSearchCondition(
                normalizedDealMode,
                trimToNull(lawdCd),
                trimToNull(sido),
                trimToNull(sigungu),
                trimToNull(umdNm),
                trimToNull(aptName),
                normalizedDealYmds.dealYmd(),
                normalizedDealYmds.startDealYmd(),
                normalizedDealYmds.endDealYmd(),
                DEFAULT_SORT,
                null,
                null,
                null,
                null,
                null,
                null,
                DEFAULT_PAGE,
                DEFAULT_SIZE,
                0
        ));
    }

    private static HouseSearchCondition validated(HouseSearchCondition condition) {
        if (!condition.hasSearchCondition()) {
            throw new IllegalArgumentException("At least one search condition is required.");
        }
        return condition;
    }

    private static DealYmdRange normalizeDealYmds(String dealYmd, String startDealYmd, String endDealYmd) {
        String normalizedDealYmd = trimToNull(dealYmd);
        String normalizedStartDealYmd = trimToNull(startDealYmd);
        String normalizedEndDealYmd = trimToNull(endDealYmd);
        if (normalizedDealYmd != null) {
            return new DealYmdRange(normalizedDealYmd, null, null);
        }
        if (normalizedStartDealYmd != null && normalizedEndDealYmd != null
                && normalizedStartDealYmd.compareTo(normalizedEndDealYmd) > 0) {
            throw new IllegalArgumentException("startDealYmd must be less than or equal to endDealYmd.");
        }
        return new DealYmdRange(null, normalizedStartDealYmd, normalizedEndDealYmd);
    }

    private static int normalizePage(Integer page) {
        return page == null || page < 1 ? DEFAULT_PAGE : page;
    }

    private static int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private static String normalizeDealMode(String dealMode) {
        String normalized = trimToNull(dealMode);
        if (normalized == null) {
            return DEFAULT_DEAL_MODE;
        }
        return switch (normalized) {
            case "sale", "jeonse", "monthly", "rent", "all" -> normalized;
            default -> throw new IllegalArgumentException("Unsupported dealMode option: " + dealMode);
        };
    }

    private static String normalizeSort(String sort, String dealMode) {
        String normalized = trimToNull(sort);
        if (normalized == null) {
            return DEFAULT_SORT;
        }
        boolean supported = switch (dealMode) {
            case "sale" -> switch (normalized) {
                case "latest", "oldest", "priceDesc", "priceAsc", "areaDesc", "areaAsc" -> true;
                default -> false;
            };
            case "jeonse" -> switch (normalized) {
                case "latest", "oldest", "depositDesc", "depositAsc", "areaDesc", "areaAsc" -> true;
                default -> false;
            };
            case "monthly" -> switch (normalized) {
                case "latest", "oldest", "depositDesc", "depositAsc", "monthlyRentDesc", "monthlyRentAsc", "areaDesc", "areaAsc" -> true;
                default -> false;
            };
            case "rent", "all" -> switch (normalized) {
                case "latest", "oldest", "areaDesc", "areaAsc" -> true;
                default -> false;
            };
            default -> false;
        };
        if (!supported) {
            throw new IllegalArgumentException("Unsupported sort option for dealMode=" + dealMode + ": " + sort);
        }
        return normalized;
    }

    private static void validatePriceFilters(
            String dealMode,
            Integer minPrice,
            Integer maxPrice,
            Integer minDeposit,
            Integer maxDeposit,
            Integer minMonthlyRent,
            Integer maxMonthlyRent
    ) {
        boolean hasSalePrice = minPrice != null || maxPrice != null;
        boolean hasDeposit = minDeposit != null || maxDeposit != null;
        boolean hasMonthlyRent = minMonthlyRent != null || maxMonthlyRent != null;
        switch (dealMode) {
            case "sale" -> {
                if (hasDeposit || hasMonthlyRent) {
                    throw new IllegalArgumentException("Rent price filters are not supported for dealMode=sale.");
                }
            }
            case "jeonse" -> {
                if (hasSalePrice || hasMonthlyRent) {
                    throw new IllegalArgumentException("Only deposit filters are supported for dealMode=jeonse.");
                }
            }
            case "monthly" -> {
                if (hasSalePrice) {
                    throw new IllegalArgumentException("Sale price filters are not supported for dealMode=monthly.");
                }
            }
            case "rent", "all" -> {
                if (hasSalePrice || hasDeposit || hasMonthlyRent) {
                    throw new IllegalArgumentException("Price filters are not supported for dealMode=" + dealMode + ".");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported dealMode option: " + dealMode);
        }
    }

    private static void validateRange(Integer min, Integer max, String minName, String maxName) {
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException(minName + " must be less than or equal to " + maxName + ".");
        }
    }

    private static Integer normalizePrice(Integer price, String fieldName) {
        if (price == null) {
            return null;
        }
        if (price < 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to 0.");
        }
        return price;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record DealYmdRange(String dealYmd, String startDealYmd, String endDealYmd) {
    }
}
