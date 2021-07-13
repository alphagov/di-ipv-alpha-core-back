package uk.gov.di.ipv.core.back.domain.data;

import lombok.Data;

import java.util.List;

@Data
public class IdentityVerificationBundle {
    private List<IdentityEvidence> identityEvidence;
    private ActivityCheck[] activityChecks;
    private FraudCheck fraudCheck;
    private IdentityVerification identityVerification;
    private BundleScores bundleScores = new BundleScores();
}
