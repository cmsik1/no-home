package com.ssafy.home.publicdata.persistence;

import com.ssafy.home.test.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaPublicDataImportAdapter.class)
class JpaPublicDataImportAdapterTest extends PostgresIntegrationTest {

    @Autowired
    private PublicDataImportPersistencePort persistencePort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void postgresUpsertsAndStoresJsonbWithoutDuplicateDeals() {
        persistencePort.upsertRequestedBatch("trade-api", "11680", "202605", "apartment", "sale");
        persistencePort.upsertRequestedBatch("trade-api", "11680", "202605", "apartment", "sale");
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM public_data_import_batches", Integer.class))
                .isEqualTo(1);

        persistencePort.upsertRegion("11680", "서울특별시", "강남구", "역삼동");
        Long regionId = persistencePort.selectRegionId("11680", "역삼동").orElseThrow();
        HouseUpsertCommand house = new HouseUpsertCommand(regionId, "11680", "역삼동", "1-1", "테스트아파트", null);
        persistencePort.upsertHouse(house);
        persistencePort.upsertHouse(house);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM houses", Integer.class)).isEqualTo(1);
        Long houseId = persistencePort.selectHouseId(house).orElseThrow();

        HouseDealInsertCommand deal = saleDeal(houseId);
        assertThat(persistencePort.insertHouseDealIfAbsent(deal)).isEqualTo(1);
        assertThat(persistencePort.insertHouseDealIfAbsent(deal)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT raw_response ->> 'apt' FROM house_deals", String.class))
                .isEqualTo("테스트아파트");
    }

    private static HouseDealInsertCommand saleDeal(Long houseId) {
        return new HouseDealInsertCommand(
                houseId, "trade-api", "11680", "202605", "sale",
                2026, 5, 10, LocalDate.of(2026, 5, 10), "120,000", 120000,
                null, null, null, null, null, new BigDecimal("84.970"), 10,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null,
                "b".repeat(64), "{\"apt\":\"테스트아파트\"}"
        );
    }
}
