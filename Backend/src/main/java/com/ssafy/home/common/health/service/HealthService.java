package com.ssafy.home.common.health.service;

import com.ssafy.home.common.health.dto.DatabaseHealth;
import com.ssafy.home.common.health.dto.HealthResponse;
import com.ssafy.home.common.health.persistence.HealthCheckPersistencePort;
import org.springframework.stereotype.Service;

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
