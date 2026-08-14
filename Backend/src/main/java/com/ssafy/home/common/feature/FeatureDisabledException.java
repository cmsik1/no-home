package com.ssafy.home.common.feature;

/** 공개 환경에서 명시적으로 차단한 기능이 호출됐음을 HTTP 경계에 전달한다. */
public final class FeatureDisabledException extends RuntimeException {

    public FeatureDisabledException() {
        super("FEATURE_DISABLED");
    }
}
