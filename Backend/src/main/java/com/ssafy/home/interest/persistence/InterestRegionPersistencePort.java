package com.ssafy.home.interest.persistence;

import com.ssafy.home.interest.dto.InterestRegion;

import java.util.List;
import java.util.Optional;

public interface InterestRegionPersistencePort {

    List<InterestRegion> selectByMemberId(Long memberId);
    Optional<Long> selectRegionId(String lawdCd, String umdNm);
    int insertRegion(String lawdCd, String legalDongCode, String sido, String sigungu, String umdNm);
    int insertInterestRegion(Long memberId, Long regionId);
    int deleteInterestRegion(Long memberId, Long interestRegionId);
}
