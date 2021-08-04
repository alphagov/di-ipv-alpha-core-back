package uk.gov.di.ipv.core.back.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSetRequest;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.di.ipv.core.back.domain.AttributeName;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.data.IdentityEvidence;
import uk.gov.di.ipv.core.back.service.AttributeService;
import uk.gov.di.ipv.core.back.util.ClaimsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class AttributeServiceImpl implements AttributeService {

    private final ObjectMapper objectMapper;

    @Autowired
    public AttributeServiceImpl(
        final ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void updateAttributesInSession(SessionData sessionData, IdentityEvidence identityEvidence) {
        // collects all attributes from evidences and saves them in session
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

    @Override
    public Optional<Map<String, Object>> aggregateAttributes(SessionData sessionData) {
        // aggregates requested attributes from the initial claims set request
        ClaimsSetRequest claimsSetRequest;

        try {
            claimsSetRequest = ClaimsUtil.getClaimsSetRequest(sessionData);
        } catch (com.nimbusds.oauth2.sdk.ParseException parseException) {
            log.error("Could not parse claims set request", parseException);
            return Optional.empty();
        }

        var aggregatedAttributes = new HashMap<String, Object>();

        claimsSetRequest.get("userinfo", null).getAdditionalInformation().forEach((additionalInfo, _val) -> {
            var attribute = AttributeName.fromString(additionalInfo);

            if (attribute == null) {
                log.warn("Could not find requested attribute, `{}`", additionalInfo);
                return;
            }

            if (attribute.equals(AttributeName.ADDRESS)) {
                // address attribute needs discussion
                var addressJson = getAddressJson(sessionData);
                if (aggregatedAttributes.containsKey(AttributeName.ADDRESS.toString())) {
                    aggregatedAttributes.merge(AttributeName.ADDRESS.toString(), addressJson, (v1, v2) -> v1 + "," + v2);
                    return;
                }

                aggregatedAttributes.put(AttributeName.ADDRESS.toString(), addressJson);
                return;
            }

            var collectedAttributeValue = sessionData.getCollectedAttributes().get(attribute);
            if (collectedAttributeValue != null && !collectedAttributeValue.isEmpty()) {
                // /userinfo should only return populated attributes
                aggregatedAttributes.put(attribute.toString(), sessionData.getCollectedAttributes().get(attribute));
            }
        });

        return Optional.of(aggregatedAttributes);
    }

    private Map<AttributeName, List<String>> collectAttributesFromEvidence(IdentityEvidence identityEvidence) {
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

    private String getAddressJson(SessionData sessionData) {
        // if address is requested, lets build an address json format until we decide
        // on the format to return multiple addresses and their corresponding values.
        // TODO: refactor this when have a chance -> update ATPs to return a single object rather than many
        //       lines of random data.
        var jsonAddressFormat = new JSONObject();
        jsonAddressFormat.appendField(AttributeName.ADDRESS_LINE_1.toString(),
            sessionData.getCollectedAttributes().get(AttributeName.ADDRESS_LINE_1));
        jsonAddressFormat.appendField(AttributeName.ADDRESS_LINE_2.toString(),
            sessionData.getCollectedAttributes().get(AttributeName.ADDRESS_LINE_2));
        jsonAddressFormat.appendField(AttributeName.ADDRESS_COUNTY.toString(),
            sessionData.getCollectedAttributes().get(AttributeName.ADDRESS_COUNTY));
        jsonAddressFormat.appendField(AttributeName.ADDRESS_TOWN.toString(),
            sessionData.getCollectedAttributes().get(AttributeName.ADDRESS_TOWN));
        jsonAddressFormat.appendField(AttributeName.ADDRESS_POSTCODE.toString(),
            sessionData.getCollectedAttributes().get(AttributeName.ADDRESS_POSTCODE));

        return jsonAddressFormat.toJSONString();
    }
}
