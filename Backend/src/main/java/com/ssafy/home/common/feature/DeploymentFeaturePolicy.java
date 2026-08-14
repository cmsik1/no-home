package com.ssafy.home.common.feature;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 배포 환경별 위험 기능 허용 여부를 한곳에서 판단한다.
 * 운영 프로필은 설정이 누락돼도 비활성화되며, 각 서비스는 외부 호출이나 데이터 변경 전에 이 정책을 확인한다.
 */
@Component
public class DeploymentFeaturePolicy {

    private final boolean passwordResetEnabled;
    private final boolean memberSearchEnabled;
    private final boolean publicDataLiveSearchEnabled;
    private final boolean publicDataManualImportEnabled;

    public DeploymentFeaturePolicy(
            @Value("${app.features.password-reset-enabled:true}") boolean passwordResetEnabled,
            @Value("${app.features.member-search-enabled:true}") boolean memberSearchEnabled,
            @Value("${app.features.public-data-live-search-enabled:true}") boolean publicDataLiveSearchEnabled,
            @Value("${app.features.public-data-manual-import-enabled:true}") boolean publicDataManualImportEnabled
    ) {
        this.passwordResetEnabled = passwordResetEnabled;
        this.memberSearchEnabled = memberSearchEnabled;
        this.publicDataLiveSearchEnabled = publicDataLiveSearchEnabled;
        this.publicDataManualImportEnabled = publicDataManualImportEnabled;
    }

    public static DeploymentFeaturePolicy allEnabled() {
        return new DeploymentFeaturePolicy(true, true, true, true);
    }

    public void requirePasswordResetEnabled() {
        requireEnabled(passwordResetEnabled);
    }

    public void requireMemberSearchEnabled() {
        requireEnabled(memberSearchEnabled);
    }

    public boolean publicDataLiveSearchEnabled() {
        return publicDataLiveSearchEnabled;
    }

    public void requirePublicDataManualImportEnabled() {
        requireEnabled(publicDataManualImportEnabled);
    }

    private static void requireEnabled(boolean enabled) {
        if (!enabled) {
            throw new FeatureDisabledException();
        }
    }
}
