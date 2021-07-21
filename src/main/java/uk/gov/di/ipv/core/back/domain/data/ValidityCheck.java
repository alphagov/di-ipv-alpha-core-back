package uk.gov.di.ipv.core.back.domain.data;

import lombok.Data;

@Data
public class ValidityCheck {
    private String original;
    private String errors;
    private String details;
    private String logos;
    private String consistent;
    private String authoritativeSource;
    private String visibleSecurityFeatures;
    private String specialistLightSecurityFeatures;
    private String physicalSecurityFeatures;
    private String cryptographicCheck;
    private String lostStolenCancelled;
    private String expired;
}
