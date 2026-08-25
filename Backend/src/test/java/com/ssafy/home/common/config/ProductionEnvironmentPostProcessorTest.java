package com.ssafy.home.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionEnvironmentPostProcessorTest {

    private static final String STRONG_SECRET = "f3a9c1e7b52d4486a0c9d2e1f6b8740c5a1e9d3b2c4f6088";

    @Test
    void ignoresNonProductionProfile() {
        ProductionEnvironmentPostProcessor processor = new ProductionEnvironmentPostProcessor();

        assertThatCode(() -> processor.postProcessEnvironment(new MockEnvironment(), null))
                .doesNotThrowAnyException();
    }

    @Test
    void reportsEveryMissingRequiredVariableWithoutPrintingValues() {
        assertThatThrownBy(() -> ProductionEnvironmentPostProcessor.validate(null, "", null, "", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB_URL")
                .hasMessageContaining("DB_USERNAME")
                .hasMessageContaining("DB_PASSWORD")
                .hasMessageContaining("auth.jwt.secret")
                .hasMessageContaining("cookie-secure");
    }

    @Test
    void rejectsNonPostgresUrlAndUnsafeSecuritySettingsWithoutEchoingSecrets() {
        String databasePassword = "database-password-must-not-leak";
        String weakSecret = "weak-secret-must-not-leak";

        assertThatThrownBy(() -> ProductionEnvironmentPostProcessor.validate(
                "https://database.example.com", "nohome", databasePassword, weakSecret, "false"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB_URL")
                .matches(exception -> !exception.getMessage().contains(databasePassword))
                .matches(exception -> !exception.getMessage().contains(weakSecret));
    }

    @Test
    void acceptsCompleteProductionContract() {
        assertThatCode(() -> ProductionEnvironmentPostProcessor.validate(
                "jdbc:postgresql://database.example.com:5432/nohome?sslmode=require",
                "nohome", "database-password", STRONG_SECRET, "true"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsNeonUrlWithoutTls() {
        assertThatThrownBy(() -> ProductionEnvironmentPostProcessor.validate(
                "jdbc:postgresql://ep-example.ap-southeast-1.aws.neon.tech/nohome?channelBinding=require",
                "nohome", "database-password", STRONG_SECRET, "true"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Neon DB_URL")
                .hasMessageContaining("sslmode");
    }

    @Test
    void rejectsNeonUrlWithoutRequiredChannelBinding() {
        assertThatThrownBy(() -> ProductionEnvironmentPostProcessor.validate(
                "jdbc:postgresql://ep-example.ap-southeast-1.aws.neon.tech/nohome?sslmode=require",
                "nohome", "database-password", STRONG_SECRET, "true"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("channelBinding=require");
    }
}
