package uk.gov.di.ipv.core.back.domain.data;

import lombok.Data;

@Data
public class FraudCheck {
    private String level;
    private String[] fraudIndicators;
}
