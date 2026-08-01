package com.ssafy.home;

import com.ssafy.home.test.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.ai.openai.api-key=context-test-key")
class HomeApplicationTest extends PostgresIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void flywayCreatesPostgresSchemaWithoutDemoAccounts() {
        List<String> tables = jdbcTemplate.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                """, String.class);

        assertThat(tables).contains(
                "regions", "houses", "house_deals", "public_data_import_batches",
                "members", "member_refresh_tokens", "notices", "interest_regions",
                "flyway_schema_history"
        );
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM members", Integer.class)).isZero();
    }
}
