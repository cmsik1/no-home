package com.ssafy.home.member.service;

import com.ssafy.home.member.auth.MemberAuthService;
import com.ssafy.home.member.dto.MemberResponse;
import com.ssafy.home.member.dto.PasswordResetRequest;
import org.springframework.stereotype.Service;

@Service
public class MemberAccountService {

    private final MemberService memberService;
    private final MemberAuthService memberAuthService;

    public MemberAccountService(MemberService memberService, MemberAuthService memberAuthService) {
        this.memberService = memberService;
        this.memberAuthService = memberAuthService;
    }

    public void resetPassword(PasswordResetRequest request) {
        MemberResponse member = memberService.resetPassword(request);
        memberAuthService.revokeMember(member.memberId());
    }

    public void deleteAccount(Long memberId) {
        memberAuthService.revokeMember(memberId);
        memberService.deleteCurrentMember(memberId);
    }
}
