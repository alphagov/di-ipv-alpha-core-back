package uk.gov.di.ipv.core.back.config;

import com.nimbusds.oauth2.sdk.id.ClientID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.di.ipv.core.back.util.KeyReader;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Configuration
public class IpvConfig {

    private @Value("${ipv.signing.key}") String signingKey;
    private @Value("${ipv.signing.cert}") String signingCert;

    @Bean("ipv-client-id")
    ClientID clientID() {
        return new ClientID("some-client-id");
    }

    @Bean("ipv-signing-key")
    Key ipvSigningKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyReader.loadKey(signingKey);
    }

    @Bean("ipv-signing-cert")
    Certificate ipvSigningCert() throws CertificateException {
        return KeyReader.loadCertFromString(signingCert);
    }

    @Bean("ipv-signing-cert-thumbprint")
    String signingCertThumbprint() throws NoSuchAlgorithmException, CertificateException {
        var md = MessageDigest.getInstance("SHA-256");
        var cert = KeyReader.loadCertFromString(signingCert);
        byte[] der = cert.getEncoded();
        md.update(der);
        byte[] digest = md.digest();
        return Base64.getUrlEncoder().encodeToString(digest).replaceAll("=", "");
    }
}
