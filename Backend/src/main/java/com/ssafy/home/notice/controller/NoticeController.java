package com.ssafy.home.notice.controller;

import com.ssafy.home.common.response.ApiResponse;
import com.ssafy.home.member.auth.CurrentMemberId;
import com.ssafy.home.notice.dto.NoticeRequest;
import com.ssafy.home.notice.dto.NoticeResponse;
import com.ssafy.home.notice.service.NoticeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 공지 목록·상세와 관리자 쓰기 요청을 {@link NoticeService}로 전달한다.
 * 인증된 회원 id는 수정 가능 여부와 관리자 권한 판단에 사용된다.
 */
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ApiResponse<List<NoticeResponse>> notices(
            @RequestParam(required = false) Integer limit,
            @CurrentMemberId Long memberId
    ) {
        return ApiResponse.ok(noticeService.findRecent(limit, memberId));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> notice(
            @PathVariable Long noticeId,
            @CurrentMemberId Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(noticeService.findById(noticeId, memberId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse>> create(
            @RequestBody NoticeRequest requestBody,
            @CurrentMemberId Long memberId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("created", noticeService.create(memberId, requestBody)));
    }

    @PutMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse>> update(
            @PathVariable Long noticeId,
            @RequestBody NoticeRequest requestBody,
            @CurrentMemberId Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(noticeService.update(memberId, noticeId, requestBody)));
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> delete(
            @PathVariable Long noticeId,
            @CurrentMemberId Long memberId
    ) {
        noticeService.delete(memberId, noticeId);
        return ResponseEntity.ok(ApiResponse.ok("deleted", Map.of("deleted", true)));
    }
}
