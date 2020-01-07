package org.cbioportal.security.spring.authentication.token.oauth2;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.stereotype.Component;

// This bean is defined here so 
// that it can be stubbed in tests.
@Component
public class JwtTokenVerifierBuilder {

    @Value("${dat.oauth2.jwkUrl:}")
    private String jwkUrl;

    public JwtTokenVerifierBuilder() {}

    public RsaVerifier build(final String kid) throws MalformedURLException, JwkException {
        final JwkProvider provider = new UrlJwkProvider(new URL(jwkUrl));
        final Jwk jwk = provider.get(kid);
        final RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();
        return new RsaVerifier(publicKey, "SHA512withRSA");
    }

}