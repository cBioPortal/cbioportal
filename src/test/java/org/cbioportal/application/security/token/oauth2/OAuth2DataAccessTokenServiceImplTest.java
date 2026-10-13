package org.cbioportal.application.security.token.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

class OAuth2DataAccessTokenServiceImplTest {

  private static final String KEY_ID = "test-key";
  private static final String ISSUER = "https://keycloak.example.org/realms/cbioportal";
  private static final String CLIENT_ID = "cbioportal";

  private static KeyPair signingKeyPair;
  private static KeyPair otherKeyPair;

  private OAuth2DataAccessTokenServiceImpl service;

  @BeforeAll
  static void generateKeys() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    signingKeyPair = generator.generateKeyPair();
    otherKeyPair = generator.generateKeyPair();
  }

  @BeforeEach
  void setUp() throws Exception {
    JwtTokenVerifierBuilder verifierBuilder = mock(JwtTokenVerifierBuilder.class);
    when(verifierBuilder.build(KEY_ID))
        .thenReturn(new RSASSAVerifier((RSAPublicKey) signingKeyPair.getPublic()));
    service = new OAuth2DataAccessTokenServiceImpl(mock(RestTemplate.class), verifierBuilder);
    ReflectionTestUtils.setField(service, "issuer", ISSUER);
    ReflectionTestUtils.setField(service, "clientId", CLIENT_ID);
  }

  @Test
  void isValidAcceptsTokenWithJwtType() throws Exception {
    assertTrue(service.isValid(token(JWSAlgorithm.RS256, JOSEObjectType.JWT, signingKeyPair)));
  }

  @Test
  void isValidAcceptsAccessTokenWithAtJwtType() throws Exception {
    assertTrue(
        service.isValid(token(JWSAlgorithm.RS256, new JOSEObjectType("at+jwt"), signingKeyPair)));
  }

  @Test
  void isValidAcceptsTokensSignedWithRs256AndRs512() throws Exception {
    assertTrue(service.isValid(token(JWSAlgorithm.RS256, JOSEObjectType.JWT, signingKeyPair)));
    assertTrue(service.isValid(token(JWSAlgorithm.RS512, JOSEObjectType.JWT, signingKeyPair)));
  }

  @Test
  void isValidRejectsTokenSignedWithOtherKey() throws Exception {
    String token = token(JWSAlgorithm.RS256, JOSEObjectType.JWT, otherKeyPair);
    assertThrows(BadCredentialsException.class, () -> service.isValid(token));
  }

  @Test
  void isValidRejectsTokenWithWrongIssuer() throws Exception {
    String token = token(claims().issuer("https://other.example.org").build());
    assertThrows(BadCredentialsException.class, () -> service.isValid(token));
  }

  @Test
  void isValidRejectsTokenWithWrongAudience() throws Exception {
    String token = token(claims().audience("other-client").build());
    assertThrows(BadCredentialsException.class, () -> service.isValid(token));
  }

  @Test
  void isValidRejectsMalformedToken() {
    assertThrows(BadCredentialsException.class, () -> service.isValid("not-a-token"));
  }

  @Test
  void getUsernameReadsSubjectFromAccessTokenWithAtJwtType() throws Exception {
    String token = token(JWSAlgorithm.RS256, new JOSEObjectType("at+jwt"), signingKeyPair);
    assertEquals("test-user", service.getUsername(token));
  }

  @Test
  void getUsernameRejectsTokenWithoutSubject() throws Exception {
    String token = token(claims().subject(null).build());
    assertThrows(BadCredentialsException.class, () -> service.getUsername(token));
  }

  private static JWTClaimsSet.Builder claims() {
    return new JWTClaimsSet.Builder().subject("test-user").issuer(ISSUER).audience(CLIENT_ID);
  }

  private static String token(JWSAlgorithm algorithm, JOSEObjectType type, KeyPair keyPair)
      throws Exception {
    return token(claims().build(), algorithm, type, keyPair);
  }

  private static String token(JWTClaimsSet claims) throws Exception {
    return token(claims, JWSAlgorithm.RS256, JOSEObjectType.JWT, signingKeyPair);
  }

  private static String token(
      JWTClaimsSet claims, JWSAlgorithm algorithm, JOSEObjectType type, KeyPair keyPair)
      throws Exception {
    JWSHeader header = new JWSHeader.Builder(algorithm).type(type).keyID(KEY_ID).build();
    SignedJWT jwt = new SignedJWT(header, claims);
    jwt.sign(new RSASSASigner(keyPair.getPrivate()));
    return jwt.serialize();
  }
}
