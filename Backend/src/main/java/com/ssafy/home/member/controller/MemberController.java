package com.ssafy.home.member.controller;

import com.ssafy.home.common.response.ApiResponse;
import com.ssafy.home.member.auth.AuthCookieService;
import com.ssafy.home.member.auth.CurrentMemberId;
import com.ssafy.home.member.auth.JwtTokenPair;
import com.ssafy.home.member.auth.MemberAuthService;
import com.ssafy.home.member.dto.MemberLoginRequest;
import com.ssafy.home.member.dto.MemberResponse;
import com.ssafy.home.member.dto.MemberSignupRequest;
import com.ssafy.home.member.dto.MemberUpdateRequest;
import com.ssafy.home.member.dto.PasswordResetRequest;
import com.ssafy.home.member.service.MemberService;
import com.ssafy.home.member.service.MemberAccountService;
import com.ssafy.home.member.service.MemberException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;
    private final MemberAuthService memberAuthService;
    private final AuthCookieService authCookieService;
    private final MemberAccountService memberAccountService;

    public MemberController(
            MemberService memberService,
            MemberAuthService memberAuthService,
            AuthCookieService authCookieService,
            MemberAccountService memberAccountService
    ) {
        this.memberService = memberService;
        this.memberAuthService = memberAuthService;
        this.authCookieService = authCookieService;
        this.memberAccountService = memberAccountService;
    }

    @PostMapping("/members")
    public ResponseEntity<ApiResponse<MemberResponse>> signup(@RequestBody MemberSignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("created", memberService.signup(request)));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<MemberResponse>> login(
            @RequestBody MemberLoginRequest request,
            HttpServletResponse response
    ) {
        MemberAuthService.LoginResult result = memberAuthService.login(
                request == null ? null : request.email(),
                request == null ? null : request.password()
        );
        authCookieService.writeTokenPair(response, result.tokens());
        return ResponseEntity.ok(ApiResponse.ok(result.member()));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            JwtTokenPair tokens = memberAuthService.refresh(authCookieService.refreshToken(request));
            authCookieService.writeTokenPair(response, tokens);
            return ResponseEntity.ok(ApiResponse.ok(Map.of("refreshed", true)));
        } catch (MemberException exception) {
            authCookieService.clear(response);
            throw exception;
        }
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Map<String, Boolean>> logout(HttpServletRequest request, HttpServletResponse response) {
        memberAuthService.logout(authCookieService.refreshToken(request));
        authCookieService.clear(response);
        return ApiResponse.ok("logged out", Map.of("loggedOut", true));
    }

    @PostMapping("/auth/password-reset")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> resetPassword(@RequestBody PasswordResetRequest request) {
        memberAccountService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("password reset", Map.of("reset", true)));
    }

    @GetMapping("/members/me")
    public ResponseEntity<ApiResponse<MemberResponse>> me(@CurrentMemberId Long memberId) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.findCurrentMember(memberId)));
    }

    @GetMapping("/members/search")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> searchMembers(
            @RequestParam String keyword,
            @CurrentMemberId Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.searchMembers(memberId, keyword)));
    }

    @PutMapping("/members/me")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMe(
            @RequestBody MemberUpdateRequest requestBody,
            @CurrentMemberId Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.updateCurrentMember(memberId, requestBody)));
    }

    @DeleteMapping("/members/me")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> deleteMe(
            @CurrentMemberId Long memberId,
            HttpServletResponse response
    ) {
        memberAccountService.deleteAccount(memberId);
        authCookieService.clear(response);
        return ResponseEntity.ok(ApiResponse.ok("deleted", Map.of("deleted", true)));
    }
}
