package com.ssafy.home.member.service;

import com.ssafy.home.member.auth.MemberAuthService;
import com.ssafy.home.member.dto.MemberResponse;
import com.ssafy.home.member.dto.PasswordResetRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberAccountServiceTest {

    private final MemberService memberService = mock(MemberService.class);
    private final MemberAuthService authService = mock(MemberAuthService.class);
    private final MemberAccountService accountService = new MemberAccountService(memberService, authService);

    @Test
    void passwordResetRevokesExistingSessions() {
        PasswordResetRequest request = new PasswordResetRequest("user@example.com", "User", "010", "new-password");
        when(memberService.resetPassword(request)).thenReturn(member(3L));

        accountService.resetPassword(request);

        var ordered = inOrder(memberService, authService);
        ordered.verify(memberService).resetPassword(request);
        ordered.verify(authService).revokeMember(3L);
    }

    @Test
    void deletionRevokesSessionBeforeRemovingMember() {
        accountService.deleteAccount(3L);

        var ordered = inOrder(authService, memberService);
        ordered.verify(authService).revokeMember(3L);
        ordered.verify(memberService).deleteCurrentMember(3L);
    }

    private static MemberResponse member(Long id) {
        return new MemberResponse(id, "user@example.com", "User", "010",
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 1, 0, 0));
    }
}
