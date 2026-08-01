package com.ssafy.home.interest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface InterestRegionRepository extends JpaRepository<InterestRegionEntity, Long> {

    @EntityGraph(attributePaths = "region")
    List<InterestRegionEntity> findAllByMemberIdOrderByRegionSidoAscRegionSigunguAscRegionUmdNmAsc(Long memberId);

    Optional<InterestRegionEntity> findByMemberIdAndRegionRegionId(Long memberId, Long regionId);

    Optional<InterestRegionEntity> findByMemberIdAndInterestRegionId(Long memberId, Long interestRegionId);
}
