package com.ssafy.home.common.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates production security settings with a fail-closed policy.
 * <p>
 * Production startup is blocked when the JWT secret is missing, obviously local,
 * too short, or when secure cookies are disabled. Error messages report only the
 * reason and never echo the configured secret value.
 */
@Component
@Profile("prod")
public class ProductionSecurityValidator implements InitializingBean {

    private final String jwtSecret;
    private final boolean cookieSecure;

    public ProductionSecurityValidator(
            @Value("${auth.jwt.secret:}") String jwtSecret,
            @Value("${auth.jwt.cookie-secure:false}") boolean cookieSecure
    ) {
        this.jwtSecret = jwtSecret;
        this.cookieSecure = cookieSecure;
    }

    @Override
    public void afterPropertiesSet() {
        validate(jwtSecret, cookieSecure);
    }

    /**
     * Validates production security values and reports only generic reasons.
     */
    static void validate(String secret, boolean cookieSecure) {
        List<String> violations = new ArrayList<>();
        if (isWeakSecret(secret)) {
            violations.add("auth.jwt.secret is missing or uses a known development default; set a strong JWT_SECRET (>= 32 chars).");
        }
        if (!cookieSecure) {
            violations.add("auth.jwt.cookie-secure must be true in production; set JWT_COOKIE_SECURE=true.");
        }
        if (!violations.isEmpty()) {
            throw new IllegalStateException(
                    "Insecure production security configuration: " + String.join(" ", violations));
        }
    }

    private static boolean isWeakSecret(String secret) {
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            return true;
        }
        String lower = secret.toLowerCase();
        return lower.contains("local-development") || lower.contains("change" + "-me");
    }
}
