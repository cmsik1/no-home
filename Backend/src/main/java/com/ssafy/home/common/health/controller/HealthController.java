package com.ssafy.home.common.health.controller;

import com.ssafy.home.common.health.dto.HealthResponse;
import com.ssafy.home.common.health.service.HealthService;
import com.ssafy.home.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 애플리케이션과 DB 연결 상태를 외부 health check가 소비할 JSON 응답으로 노출한다.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<HealthResponse>> health() {
        HealthResponse health = healthService.check();
        if (health.database().connected()) {
            return ResponseEntity.ok(ApiResponse.ok(health));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail("application is running, but database check failed", health));
    }
}
