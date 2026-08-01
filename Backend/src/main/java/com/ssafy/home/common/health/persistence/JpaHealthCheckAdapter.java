package com.ssafy.home.common.health.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class JpaHealthCheckAdapter implements HealthCheckPersistencePort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Integer selectDatabaseProbe() {
        return ((Number) entityManager.createNativeQuery("SELECT 1").getSingleResult()).intValue();
    }
}
