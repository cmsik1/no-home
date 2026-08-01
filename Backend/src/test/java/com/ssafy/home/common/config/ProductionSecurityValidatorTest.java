package com.ssafy.home.common.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionSecurityValidatorTest {

    // Mirrors an obvious local-development marker that must never pass in prod.
    private static final String DEV_DEFAULT = "no-home-local-development-jwt-secret-placeholder-32bytes";
    // Long random-looking secret without development markers.
    private static final String STRONG_SECRET = "f3a9c1e7b52d4486a0c9d2e1f6b8740c5a1e9d3b2c4f6088";

    @Test
    void rejectsDevelopmentDefaultSecret() {
        assertThatThrownBy(() -> ProductionSecurityValidator.validate(DEV_DEFAULT, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("auth.jwt.secret")
                .matches(e -> !e.getMessage().contains(DEV_DEFAULT), "message must not contain the secret value");
    }

    @Test
    void rejectsNonSecureCookie() {
        assertThatThrownBy(() -> ProductionSecurityValidator.validate(STRONG_SECRET, false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cookie-secure");
    }

    @Test
    void passesWithStrongSecretAndSecureCookie() {
        assertThatCode(() -> ProductionSecurityValidator.validate(STRONG_SECRET, true))
                .doesNotThrowAnyException();
    }

    @Test
    void reportsBothViolationsWhenSecretWeakAndCookieInsecure() {
        assertThatThrownBy(() -> ProductionSecurityValidator.validate(DEV_DEFAULT, false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("auth.jwt.secret")
                .hasMessageContaining("cookie-secure");
    }
}
