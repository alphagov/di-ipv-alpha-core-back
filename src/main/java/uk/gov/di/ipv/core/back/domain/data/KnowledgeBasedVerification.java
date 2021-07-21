package uk.gov.di.ipv.core.back.domain.data;

import lombok.Data;

@Data
public class KnowledgeBasedVerification {
    private Quality quality;
    private String question;
    private String response;
}
