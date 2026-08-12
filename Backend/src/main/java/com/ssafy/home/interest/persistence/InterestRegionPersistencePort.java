package com.ssafy.home.interest.persistence;

import com.ssafy.home.interest.dto.InterestRegion;

import java.util.List;
import java.util.Optional;

/** 관심 지역 유스케이스가 사용하는 회원-지역 관계 조회·쓰기 계약이다. */
public interface InterestRegionPersistencePort {

    List<InterestRegion> selectByMemberId(Long memberId);
    Optional<Long> selectRegionId(String lawdCd, String umdNm);
    int insertRegion(String lawdCd, String legalDongCode, String sido, String sigungu, String umdNm);
    int insertInterestRegion(Long memberId, Long regionId);
    int deleteInterestRegion(Long memberId, Long interestRegionId);
}
