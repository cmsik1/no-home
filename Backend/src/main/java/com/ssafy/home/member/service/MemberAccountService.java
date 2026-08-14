package com.ssafy.home.member.service;

import com.ssafy.home.common.feature.DeploymentFeaturePolicy;
import com.ssafy.home.member.auth.MemberAuthService;
import com.ssafy.home.member.dto.MemberResponse;
import com.ssafy.home.member.dto.PasswordResetRequest;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 여러 component를 함께 변경하는 계정 유스케이스를 조율한다.
 * 비밀번호 재설정과 회원 탈퇴 시 회원 정보와 인증 token 상태가 어긋나지 않도록 service 호출 순서를 관리한다.
 */
@Service
public class MemberAccountService {

    private final MemberService memberService;
    private final MemberAuthService memberAuthService;
    private final DeploymentFeaturePolicy featurePolicy;

    @Autowired
    public MemberAccountService(MemberService memberService, MemberAuthService memberAuthService,
                                DeploymentFeaturePolicy featurePolicy) {
        this.memberService = memberService;
        this.memberAuthService = memberAuthService;
        this.featurePolicy = featurePolicy;
    }

    public MemberAccountService(MemberService memberService, MemberAuthService memberAuthService) {
        this(memberService, memberAuthService, DeploymentFeaturePolicy.allEnabled());
    }

    public void resetPassword(PasswordResetRequest request) {
        featurePolicy.requirePasswordResetEnabled();
        MemberResponse member = memberService.resetPassword(request);
        memberAuthService.revokeMember(member.memberId());
    }

    public void deleteAccount(Long memberId) {
        memberAuthService.revokeMember(memberId);
        memberService.deleteCurrentMember(memberId);
    }
}
