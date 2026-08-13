package com.ssafy.home.common.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates production security settings with a fail-closed policy.
 * <p>
 * Production startup is blocked when the JWT secret is missing, obviously local,
 * too short, or when secure cookies are disabled. Error messages report only the
 * reason and never echo the configured secret value.
 */
final class ProductionSecurityValidator {

    private ProductionSecurityValidator() {
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
