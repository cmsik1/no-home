package com.ssafy.home.common.feature;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeploymentFeaturePolicyTest {

    @Test
    void disabledOperationsFailWithStablePublicMessage() {
        DeploymentFeaturePolicy policy = new DeploymentFeaturePolicy(false, false, false, false);

        assertThatThrownBy(policy::requirePasswordResetEnabled)
                .isInstanceOf(FeatureDisabledException.class)
                .hasMessage("FEATURE_DISABLED");
        assertThatThrownBy(policy::requireMemberSearchEnabled)
                .isInstanceOf(FeatureDisabledException.class);
        assertThatThrownBy(policy::requirePublicDataManualImportEnabled)
                .isInstanceOf(FeatureDisabledException.class);
        assertThat(policy.publicDataLiveSearchEnabled()).isFalse();
    }

    @Test
    void localPolicyAllowsExistingBehavior() {
        DeploymentFeaturePolicy policy = DeploymentFeaturePolicy.allEnabled();

        policy.requirePasswordResetEnabled();
        policy.requireMemberSearchEnabled();
        policy.requirePublicDataManualImportEnabled();
        assertThat(policy.publicDataLiveSearchEnabled()).isTrue();
    }
}
