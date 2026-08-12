package com.ssafy.home.interest.service;

import com.ssafy.home.common.region.SeoulLawdCodeResolver;
import com.ssafy.home.common.region.SeoulLegalDongCatalog;
import com.ssafy.home.house.dto.RegionResponse;
import com.ssafy.home.interest.dto.InterestRegionRequest;
import com.ssafy.home.interest.dto.InterestRegionResponse;
import com.ssafy.home.interest.persistence.InterestRegionPersistencePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회원별 관심 지역 규칙과 트랜잭션을 담당한다.
 * 외부 요청의 법정동 코드를 실제 region id로 해석한 뒤 회원-지역 관계를 persistence port에 저장한다.
 */
@Service
@Transactional(readOnly = true)
public class InterestRegionService {

    private final InterestRegionPersistencePort interestRegionPersistencePort;
    private final SeoulLawdCodeResolver seoulLawdCodeResolver;

    public InterestRegionService(InterestRegionPersistencePort interestRegionPersistencePort, SeoulLawdCodeResolver seoulLawdCodeResolver) {
        this.interestRegionPersistencePort = interestRegionPersistencePort;
        this.seoulLawdCodeResolver = seoulLawdCodeResolver;
    }

    public List<InterestRegionResponse> findMyRegions(Long memberId) {
        requireLogin(memberId);
        return interestRegionPersistencePort.selectByMemberId(memberId).stream()
                .map(InterestRegionResponse::from)
                .toList();
    }

    @Transactional
    public InterestRegionResponse addMyRegion(Long memberId, InterestRegionRequest request) {
        requireLogin(memberId);
        RegionResponse region = resolveRegion(request);
        Long regionId = interestRegionPersistencePort.selectRegionId(region.lawdCd(), region.umdNm())
                .orElseGet(() -> insertAndFindRegion(region));
        interestRegionPersistencePort.insertInterestRegion(memberId, regionId);
        return interestRegionPersistencePort.selectByMemberId(memberId).stream()
                .filter(interestRegion -> regionId.equals(interestRegion.regionId()))
                .findFirst()
                .map(InterestRegionResponse::from)
                .orElseThrow(() -> new InterestRegionException(InterestRegionErrorCode.NOT_FOUND, "interest region not found."));
    }

    @Transactional
    public void deleteMyRegion(Long memberId, Long interestRegionId) {
        requireLogin(memberId);
        if (interestRegionId == null) {
            throw new InterestRegionException(InterestRegionErrorCode.VALIDATION, "interestRegionId is required.");
        }
        if (interestRegionPersistencePort.deleteInterestRegion(memberId, interestRegionId) == 0) {
            throw new InterestRegionException(InterestRegionErrorCode.NOT_FOUND, "interest region not found.");
        }
    }

    private Long insertAndFindRegion(RegionResponse region) {
        interestRegionPersistencePort.insertRegion(region.lawdCd(), region.legalDongCode(), region.sido(), region.sigungu(), region.umdNm());
        return interestRegionPersistencePort.selectRegionId(region.lawdCd(), region.umdNm())
                .orElseThrow(() -> new InterestRegionException(InterestRegionErrorCode.NOT_FOUND, "region not found."));
    }

    private RegionResponse resolveRegion(InterestRegionRequest request) {
        String lawdCd = trimToNull(request == null ? null : request.lawdCd());
        String umdNm = trimToNull(request == null ? null : request.umdNm());
        if (lawdCd == null) {
            lawdCd = seoulLawdCodeResolver.resolveLawdCds(
                    null,
                    trimToNull(request == null ? null : request.sido()),
                    trimToNull(request == null ? null : request.sigungu())
            ).stream().findFirst().orElse(null);
        }
        if (lawdCd == null || umdNm == null) {
            throw new InterestRegionException(InterestRegionErrorCode.VALIDATION, "lawdCd and umdNm are required.");
        }

        String finalUmdNm = umdNm;
        return SeoulLegalDongCatalog.regions(lawdCd, seoulLawdCodeResolver).stream()
                .filter(region -> finalUmdNm.equals(region.umdNm()))
                .findFirst()
                .orElseThrow(() -> new InterestRegionException(InterestRegionErrorCode.VALIDATION, "unsupported interest region."));
    }

    private static void requireLogin(Long memberId) {
        if (memberId == null) {
            throw new InterestRegionException(InterestRegionErrorCode.UNAUTHENTICATED, "login required.");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
