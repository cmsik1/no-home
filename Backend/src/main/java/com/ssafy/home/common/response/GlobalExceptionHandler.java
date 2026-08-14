package com.ssafy.home.common.response;

import com.ssafy.home.house.service.AutoImportException;
import com.ssafy.home.common.feature.FeatureDisabledException;
import com.ssafy.home.interest.service.InterestRegionException;
import com.ssafy.home.member.service.MemberException;
import com.ssafy.home.notice.service.NoticeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * service 계층의 기능별 예외를 HTTP 상태와 공통 {@link ApiResponse} 실패 형식으로 변환한다.
 * Controller마다 같은 try/catch와 상태 변환을 반복하지 않게 하는 HTTP 경계다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeatureDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeatureDisabled(FeatureDisabledException exception) {
        return failure(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ApiResponse<Void>> handleMember(MemberException exception) {
        HttpStatus status = switch (exception.errorCode()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case DUPLICATE_EMAIL -> HttpStatus.CONFLICT;
            case INVALID_CREDENTIALS, UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
        };
        return failure(status, exception.getMessage());
    }

    @ExceptionHandler(InterestRegionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInterestRegion(InterestRegionException exception) {
        HttpStatus status = switch (exception.errorCode()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
        };
        return failure(status, exception.getMessage());
    }

    @ExceptionHandler(NoticeException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotice(NoticeException exception) {
        HttpStatus status = switch (exception.errorCode()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
        };
        return failure(status, exception.getMessage());
    }

    @ExceptionHandler(AutoImportException.class)
    public ResponseEntity<ApiResponse<Void>> handleAutoImport(AutoImportException exception) {
        HttpStatus status = switch (exception.reason()) {
            case KEY_MISSING, KEY_INVALID, QUOTA -> HttpStatus.SERVICE_UNAVAILABLE;
            case TIMEOUT -> HttpStatus.GATEWAY_TIMEOUT;
            case PROVIDER_ERROR, UNKNOWN -> HttpStatus.BAD_GATEWAY;
        };
        return failure(status, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return failure(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private static ResponseEntity<ApiResponse<Void>> failure(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.fail(message, null));
    }
}
