package uk.gov.di.ipv.core.back.domain.data;

import lombok.Data;
import uk.gov.di.ipv.core.back.domain.gpg45.Score;

@Data
public class BundleScores {
    private Score activityCheckScore = Score.NOT_AVAILABLE;
    private Score fraudCheckScore = Score.NOT_AVAILABLE;
    private Score identityVerificationScore = Score.NOT_AVAILABLE;
}
