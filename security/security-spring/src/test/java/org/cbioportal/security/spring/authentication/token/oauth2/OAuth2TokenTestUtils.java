package org.cbioportal.security.spring.authentication.token.oauth2;

import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;

public class OAuth2TokenTestUtils {

    public static String createJwt(String claimsJson) {
        MacSigner signer = new MacSigner("dummy_private_key");
        return JwtHelper.encode(claimsJson, signer).getEncoded();
    }

}