package uk.gov.di.ipv.core.back.service;

import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;

import java.util.Map;
import java.util.Optional;

public interface AttributeService {

    void updateAttributesInSession(SessionData sessionData, IdentityEvidence identityEvidence);

    Optional<Map<String, Object>> aggregateAttributes(SessionData sessionData);
}
