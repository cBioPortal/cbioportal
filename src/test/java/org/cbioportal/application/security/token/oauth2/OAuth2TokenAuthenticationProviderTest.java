package org.cbioportal.application.security.token.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPairGenerator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

class OAuth2TokenAuthenticationProviderTest {

  private static final String OFFLINE_TOKEN = "offline-token";

  private OAuth2TokenRefreshRestTemplate tokenRefreshRestTemplate;
  private OAuth2TokenAuthenticationProvider provider;

  @BeforeEach
  void setUp() {
    tokenRefreshRestTemplate = mock(OAuth2TokenRefreshRestTemplate.class);
    provider = new OAuth2TokenAuthenticationProvider(tokenRefreshRestTemplate);
    ReflectionTestUtils.setField(provider, "jwtRolesPath", "resource_access::cbioportal::roles");
  }

  @Test
  void authenticateReadsUsernameAndRolesFromAccessTokenWithAtJwtType() throws Exception {
    JWTClaimsSet claims =
        new JWTClaimsSet.Builder()
            .subject("test-user")
            .claim(
                "resource_access",
                Map.of("cbioportal", Map.of("roles", List.of("study_es_0", "acc_tcga"))))
            .build();
    when(tokenRefreshRestTemplate.getAccessToken(OFFLINE_TOKEN))
        .thenReturn(token(claims, new JOSEObjectType("at+jwt")));

    Authentication authentication =
        provider.authenticate(new OAuth2BearerAuthenticationToken(OFFLINE_TOKEN));

    assertEquals("test-user", authentication.getPrincipal());
    Set<String> authorities =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    assertEquals(Set.of("ROLE_study_es_0", "ROLE_acc_tcga"), authorities);
  }

  @Test
  void authenticateRejectsAccessTokenWithoutSubject() throws Exception {
    when(tokenRefreshRestTemplate.getAccessToken(OFFLINE_TOKEN))
        .thenReturn(token(new JWTClaimsSet.Builder().build(), JOSEObjectType.JWT));

    assertThrows(
        BadCredentialsException.class,
        () -> provider.authenticate(new OAuth2BearerAuthenticationToken(OFFLINE_TOKEN)));
  }

  @Test
  void authenticateRejectsMalformedAccessToken() {
    when(tokenRefreshRestTemplate.getAccessToken(OFFLINE_TOKEN)).thenReturn("not-a-token");

    assertThrows(
        BadCredentialsException.class,
        () -> provider.authenticate(new OAuth2BearerAuthenticationToken(OFFLINE_TOKEN)));
  }

  private static String token(JWTClaimsSet claims, JOSEObjectType type) throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    SignedJWT jwt =
        new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).type(type).build(), claims);
    jwt.sign(new RSASSASigner(generator.generateKeyPair().getPrivate()));
    return jwt.serialize();
  }
}
