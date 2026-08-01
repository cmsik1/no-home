package com.ssafy.home.interest.persistence;

import com.ssafy.home.interest.dto.InterestRegion;
import com.ssafy.home.interest.repository.InterestRegionEntity;
import com.ssafy.home.interest.repository.InterestRegionRepository;
import com.ssafy.home.interest.repository.RegionEntity;
import com.ssafy.home.interest.repository.RegionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaInterestRegionAdapter implements InterestRegionPersistencePort {

    private final RegionRepository regionRepository;
    private final InterestRegionRepository interestRegionRepository;

    public JpaInterestRegionAdapter(RegionRepository regionRepository, InterestRegionRepository interestRegionRepository) {
        this.regionRepository = regionRepository;
        this.interestRegionRepository = interestRegionRepository;
    }

    @Override
    public List<InterestRegion> selectByMemberId(Long memberId) {
        return interestRegionRepository.findAllByMemberIdOrderByRegionSidoAscRegionSigunguAscRegionUmdNmAsc(memberId)
                .stream()
                .map(JpaInterestRegionAdapter::toInterestRegion)
                .toList();
    }

    @Override
    public Optional<Long> selectRegionId(String lawdCd, String umdNm) {
        return regionRepository.findByLawdCdAndUmdNm(lawdCd, umdNm).map(RegionEntity::getRegionId);
    }

    @Override
    public int insertRegion(String lawdCd, String legalDongCode, String sido, String sigungu, String umdNm) {
        RegionEntity region = regionRepository.findByLawdCdAndUmdNm(lawdCd, umdNm)
                .map(existing -> {
                    existing.update(legalDongCode, sido, sigungu);
                    return existing;
                })
                .orElseGet(() -> new RegionEntity(lawdCd, legalDongCode, sido, sigungu, umdNm));
        regionRepository.saveAndFlush(region);
        return 1;
    }

    @Override
    public int insertInterestRegion(Long memberId, Long regionId) {
        if (interestRegionRepository.findByMemberIdAndRegionRegionId(memberId, regionId).isPresent()) {
            return 0;
        }
        RegionEntity region = regionRepository.findById(regionId).orElseThrow();
        interestRegionRepository.saveAndFlush(new InterestRegionEntity(memberId, region));
        return 1;
    }

    @Override
    public int deleteInterestRegion(Long memberId, Long interestRegionId) {
        return interestRegionRepository.findByMemberIdAndInterestRegionId(memberId, interestRegionId)
                .map(interestRegion -> {
                    interestRegionRepository.delete(interestRegion);
                    return 1;
                })
                .orElse(0);
    }

    private static InterestRegion toInterestRegion(InterestRegionEntity entity) {
        RegionEntity region = entity.getRegion();
        return new InterestRegion(
                entity.getInterestRegionId(), entity.getMemberId(), region.getRegionId(),
                region.getLawdCd(), region.getLegalDongCode(), region.getSido(), region.getSigungu(),
                region.getUmdNm(), entity.getCreatedAt()
        );
    }
}
