package com.ssafy.home.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * 운영 profile이 활성화되면 외부 시스템에 연결하기 전에 필수 환경변수를 검증한다.
 * 오류에는 변수명과 정책만 기록하고 URL credential, 비밀번호와 JWT 원문은 노출하지 않는다.
 */
public class ProductionEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public int getOrder() {
        // ConfigData와 profile별 properties가 로드된 뒤, 자동 구성 Bean 생성 전 검증한다.
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.matchesProfiles("prod")) {
            return;
        }
        validate(
                environment.getProperty("DB_URL"),
                environment.getProperty("DB_USERNAME"),
                environment.getProperty("DB_PASSWORD"),
                environment.getProperty("JWT_SECRET"),
                environment.getProperty("JWT_COOKIE_SECURE"));
    }

    static void validate(
            String databaseUrl,
            String databaseUsername,
            String databasePassword,
            String jwtSecret,
            String jwtCookieSecure
    ) {
        List<String> violations = new ArrayList<>();
        if (!hasText(databaseUrl)) {
            violations.add("DB_URL is required");
        } else if (!databaseUrl.startsWith("jdbc:postgresql://")) {
            violations.add("DB_URL must be a PostgreSQL JDBC URL");
        } else if (isNeonUrl(databaseUrl) && !usesTls(databaseUrl)) {
            violations.add("Neon DB_URL must enable TLS with sslmode=require or stronger");
        }
        if (!hasText(databaseUsername)) {
            violations.add("DB_USERNAME is required");
        }
        if (!hasText(databasePassword)) {
            violations.add("DB_PASSWORD is required");
        }
        try {
            ProductionSecurityValidator.validate(jwtSecret, Boolean.parseBoolean(jwtCookieSecure));
        } catch (IllegalStateException exception) {
            violations.add(exception.getMessage());
        }
        if (!violations.isEmpty()) {
            throw new IllegalStateException("Invalid production environment: " + String.join("; ", violations));
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean isNeonUrl(String databaseUrl) {
        return databaseUrl.toLowerCase().contains(".neon.tech");
    }

    private static boolean usesTls(String databaseUrl) {
        String normalized = databaseUrl.toLowerCase();
        return normalized.matches(".*[?&]sslmode=(require|verify-ca|verify-full)(&.*)?$");
    }
}
