package uk.gov.di.ipv.core.back.util;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.claims.ClaimsSetRequest;
import net.minidev.json.JSONObject;
import uk.gov.di.ipv.core.back.domain.SessionData;
import uk.gov.di.ipv.core.back.domain.gpg45.ConfidenceLevel;

public abstract class ClaimsUtil {

    public static ClaimsSetRequest getClaimsSetRequest(SessionData sessionData) throws ParseException {
        return ClaimsSetRequest.parse(sessionData.getAuthData().getRequestedAttributes().get(0));
    }

    public static ConfidenceLevel getRequestedLevelOfConfidence(SessionData sessionData) throws ParseException {
        var claimsSetRequest = getClaimsSetRequest(sessionData);
        var claimsMap = claimsSetRequest.get("userinfo", null)
            .getAdditionalInformation();


        if (!claimsMap.containsKey("level-of-confidence")) {
            // TODO: throw an error or set a default level of confidence?
            return ConfidenceLevel.HIGH;
        }

        var data = (JSONObject) claimsMap.get("level-of-confidence");
        return ConfidenceLevel.fromString(data.get("value").toString());
    }
}
