package uk.gov.di.ipv.core.back.service;

import uk.gov.di.ipv.core.back.domain.AttributeName;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;

import java.util.List;
import java.util.Map;

public interface AttributeCollectionService {

    Map<AttributeName, List<String>> collectAttributesFromEvidence(IdentityEvidence identityEvidence);

    void updateAttributesInSession(SessionData sessionData, IdentityEvidence identityEvidence);
}
