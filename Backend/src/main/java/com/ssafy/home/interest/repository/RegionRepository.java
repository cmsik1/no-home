package com.ssafy.home.interest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<RegionEntity, Long> {

    Optional<RegionEntity> findByLawdCdAndUmdNm(String lawdCd, String umdNm);
}
