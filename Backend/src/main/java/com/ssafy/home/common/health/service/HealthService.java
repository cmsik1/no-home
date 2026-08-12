package com.ssafy.home.common.health.service;

import com.ssafy.home.common.health.dto.DatabaseHealth;
import com.ssafy.home.common.health.dto.HealthResponse;
import com.ssafy.home.common.health.persistence.HealthCheckPersistencePort;
import org.springframework.stereotype.Service;

/**
 * HTTP와 JPA 세부사항을 분리한 health 확인 유스케이스다.
 * persistence port의 probe 결과를 운영 도구가 이해할 {@code HealthResponse}로 조립한다.
 */
@Service
public class HealthService {

    private final HealthCheckPersistencePort healthCheckPersistencePort;

    public HealthService(HealthCheckPersistencePort healthCheckPersistencePort) {
        this.healthCheckPersistencePort = healthCheckPersistencePort;
    }

    public HealthResponse check() {
        try {
            Integer probe = healthCheckPersistencePort.selectDatabaseProbe();
            return new HealthResponse("UP", DatabaseHealth.connected(probe));
        } catch (RuntimeException exception) {
            return new HealthResponse("DEGRADED", DatabaseHealth.disconnected(exception.getClass().getSimpleName()));
        }
    }
}
