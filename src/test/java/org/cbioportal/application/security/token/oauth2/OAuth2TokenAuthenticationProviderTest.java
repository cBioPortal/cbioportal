package org.cbioportal.application.security.token.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.util.ReflectionTestUtils;

public class OAuth2TokenAuthenticationProviderTest {

  private static final String OFFLINE_TOKEN = "offline-token";

  private OAuth2TokenRefreshRestTemplate refreshRestTemplate;
  private OAuth2TokenAuthenticationProvider provider;

  @BeforeEach
  void setUp() {
    refreshRestTemplate = Mockito.mock(OAuth2TokenRefreshRestTemplate.class);
    provider = new OAuth2TokenAuthenticationProvider(refreshRestTemplate);
    ReflectionTestUtils.setField(provider, "jwtRolesPath", "realm_access::roles");
  }

  /** An unsigned JWT; the provider trusts the access token returned by the IdP. */
  public static String accessToken(String claimsJson) {
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    return encoder.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8))
        + "."
        + encoder.encodeToString(claimsJson.getBytes(StandardCharsets.UTF_8))
        + "."
        + encoder.encodeToString("signature".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void supportsOnlyBearerTokens() {
    assertTrue(provider.supports(OAuth2BearerAuthenticationToken.class));
    assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void authenticatesSubjectWithRolesFromTheAccessToken() {
    Mockito.when(refreshRestTemplate.getAccessToken(OFFLINE_TOKEN))
        .thenReturn(
            accessToken(
                "{\"sub\": \"user-id\", \"realm_access\": {\"roles\": [\"PUBLIC\", \"TEAM_A\"]}}"));

    Authentication result =
        provider.authenticate(new OAuth2BearerAuthenticationToken(OFFLINE_TOKEN));

    assertTrue(result.isAuthenticated());
    assertEquals("user-id", result.getPrincipal());
    assertEquals(
        Set.of("ROLE_PUBLIC", "ROLE_TEAM_A"),
        AuthorityUtils.authorityListToSet(result.getAuthorities()));
  }

  @Test
  void accessTokenWithoutRolesAuthenticatesWithNoAuthorities() {
    Mockito.when(refreshRestTemplate.getAccessToken(OFFLINE_TOKEN))
        .thenReturn(accessToken("{\"sub\": \"user-id\"}"));

    Authentication result =
        provider.authenticate(new OAuth2BearerAuthenticationToken(OFFLINE_TOKEN));

    assertTrue(result.isAuthenticated());
    assertTrue(result.getAuthorities().isEmpty());
  }

  @Test
  void accessTokenWithoutSubjectIsRejected() {
    Mockito.when(refreshRestTemplate.getAccessToken(OFFLINE_TOKEN))
        .thenReturn(accessToken("{\"realm_access\": {\"roles\": [\"PUBLIC\"]}}"));

    assertThrows(
        BadCredentialsException.class,
        () -> provider.authenticate(new OAuth2BearerAuthenticationToken(OFFLINE_TOKEN)));
  }

  @Test
  void refreshFailureIsRejected() {
    Mockito.when(refreshRestTemplate.getAccessToken(OFFLINE_TOKEN))
        .thenThrow(new BadCredentialsException("refresh failed"));

    assertThrows(
        BadCredentialsException.class,
        () -> provider.authenticate(new OAuth2BearerAuthenticationToken(OFFLINE_TOKEN)));
  }
}
