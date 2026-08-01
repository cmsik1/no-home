package com.ssafy.home.common.health.persistence;

public interface HealthCheckPersistencePort {
    Integer selectDatabaseProbe();
}
