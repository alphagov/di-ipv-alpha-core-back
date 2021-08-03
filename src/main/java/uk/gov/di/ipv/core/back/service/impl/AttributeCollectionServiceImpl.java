package uk.gov.di.ipv.core.back.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.di.ipv.core.back.domain.AttributeName;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;
import uk.gov.di.ipv.core.back.service.AttributeCollectionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AttributeCollectionServiceImpl implements AttributeCollectionService {

    private final ObjectMapper objectMapper;

    @Autowired
    public AttributeCollectionServiceImpl(
        final ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<AttributeName, List<String>> collectAttributesFromEvidence(IdentityEvidence identityEvidence) {
        var evidenceData = objectMapper.convertValue(identityEvidence.getEvidenceData(), Map.class);
        var collectedAttributes = new HashMap<AttributeName, List<String>>();

        Arrays.stream(AttributeName.values()).forEach(attribute -> {
            var attributeName = attribute.toString();
            if (evidenceData.containsKey(attributeName)) {
                // using a list as an attribute may have multiple values
                var attributeValueList = new ArrayList<String>();
                attributeValueList.add(evidenceData.get(attributeName).toString());
                collectedAttributes.put(attribute, attributeValueList);
            }
        });

        return collectedAttributes;
    }

    @Override
    public void updateAttributesInSession(SessionData sessionData, IdentityEvidence identityEvidence) {
        var collectedAttributes = collectAttributesFromEvidence(identityEvidence);
        var currentAttributes = sessionData.getCollectedAttributes();
        collectedAttributes.forEach((collectedAttributeName, collectedValue) -> {
            var combined = currentAttributes.compute(collectedAttributeName, (currentAttribute, currentValue) -> {
                // combine collected attribute values together
                if (currentValue == null) {
                    return collectedValue;
                }
                return Stream.concat(currentValue.stream(), collectedValue.stream()).collect(Collectors.toList());
            });

            currentAttributes.replace(collectedAttributeName, combined);
        });
    }
}
