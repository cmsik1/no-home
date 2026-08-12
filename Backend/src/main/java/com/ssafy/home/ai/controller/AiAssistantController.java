package com.ssafy.home.ai.controller;

import com.ssafy.home.ai.assistant.AiAssistantResult;
import com.ssafy.home.ai.assistant.AiAssistantService;
import com.ssafy.home.ai.assistant.AssistantRequest;
import com.ssafy.home.ai.assistant.AssistantResponse;
import com.ssafy.home.common.response.ApiResponse;
import com.ssafy.home.member.auth.CurrentMemberId;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 통합 AI 요청의 HTTP 경계다. service 결과 상태를 HTTP status와 공통 API 응답으로 바꾸고
 * rate limit 거절에는 클라이언트 재시도 시점을 {@code Retry-After} header로 전달한다.
 */
@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    private final AiAssistantService assistantService;

    public AiAssistantController(AiAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/assistant")
    public ResponseEntity<ApiResponse<AssistantResponse>> assistant(
            @RequestBody(required = false) AssistantRequest request,
            @CurrentMemberId Long memberId
    ) {
        AiAssistantResult result = assistantService.assist(request, memberId);
        if (result.status() == AiAssistantResult.Status.OK) {
            return ResponseEntity.ok(ApiResponse.ok(result.response()));
        }

        ResponseEntity.BodyBuilder response = ResponseEntity.status(toHttpStatus(result.status()));
        if (result.retryAfterSeconds() != null) {
            response.header(HttpHeaders.RETRY_AFTER, Long.toString(result.retryAfterSeconds()));
        }
        return response.body(ApiResponse.fail(result.message(), null));
    }

    private static HttpStatus toHttpStatus(AiAssistantResult.Status status) {
        return switch (status) {
            case OK -> HttpStatus.OK;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case CONFLICT -> HttpStatus.CONFLICT;
            case TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case GATEWAY_TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
        };
    }
}
