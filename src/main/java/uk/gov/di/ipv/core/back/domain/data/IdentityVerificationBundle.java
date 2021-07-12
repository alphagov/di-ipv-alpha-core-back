package uk.gov.di.ipv.core.back.domain.data;

import lombok.Data;

@Data
public class IdentityVerificationBundle {
    private IdentityEvidence[] identityEvidence;
    private ActivityCheck[] activityChecks;
    private FraudCheck fraudCheck;
    private IdentityVerification identityVerification;
    private BundleScores bundleScores = new BundleScores();
}
