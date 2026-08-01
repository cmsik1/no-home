package com.ssafy.home.house.persistence;

import com.ssafy.home.house.dto.HouseDealPriceRangeResponse;
import com.ssafy.home.house.dto.HouseSearchCondition;
import com.ssafy.home.test.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaHouseQueryAdapter.class)
class JpaHouseQueryAdapterTest extends PostgresIntegrationTest {

    @Autowired
    private HousePersistencePort persistencePort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void searchesCountsAndAggregatesNativeHouseDeals() {
        jdbcTemplate.update("""
                INSERT INTO regions (lawd_cd, legal_dong_code, sido, sigungu, umd_nm)
                VALUES ('11590', '1159010500', '서울특별시', '동작구', '흑석동')
                """);
        Long regionId = jdbcTemplate.queryForObject(
                "SELECT region_id FROM regions WHERE lawd_cd = '11590' AND umd_nm = '흑석동'", Long.class);
        jdbcTemplate.update("""
                INSERT INTO houses (region_id, sgg_cd, umd_nm, jibun, apt_nm, build_year)
                VALUES (?, '11590', '흑석동', '1-1', '테스트아파트', 2020)
                """, regionId);
        Long houseId = jdbcTemplate.queryForObject("SELECT house_id FROM houses WHERE apt_nm = '테스트아파트'", Long.class);
        jdbcTemplate.update("""
                INSERT INTO house_deals (
                    house_id, source_api, lawd_cd, deal_ymd, house_type, deal_type,
                    deal_year, deal_month, deal_day, deal_date, deal_amount, deal_amount_manwon, api_row_hash
                ) VALUES (?, 'test-api', '11590', '202605', 'apartment', 'sale',
                          2026, 5, 10, '2026-05-10', '120,000', 120000, ?)
                """, houseId, "a".repeat(64));

        HouseSearchCondition condition = new HouseSearchCondition(
                "sale", "11590", null, null, null, null, "202605", null, null,
                "latest", 100000, 130000, null, null, null, null, 0, 20, 0
        );

        assertThat(persistencePort.countHouseDeals(condition)).isEqualTo(1);
        assertThat(persistencePort.searchHouseDeals(condition))
                .singleElement()
                .extracting(result -> result.aptNm())
                .isEqualTo("테스트아파트");
        HouseDealPriceRangeResponse range = persistencePort.selectHouseDealPriceRange(condition);
        assertThat(range.minDealAmountManwon()).isEqualTo(120000);
        assertThat(range.maxDealAmountManwon()).isEqualTo(120000);
    }
}
