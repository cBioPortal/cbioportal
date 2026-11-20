package org.cbioportal.application.security.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cbioportal.application.security.token.config.DataAccessTokenConfig;
import org.cbioportal.application.security.token.oauth2.JwtTokenVerifierBuilder;
import org.cbioportal.application.security.token.oauth2.OAuth2TokenAuthenticationProviderTest;
import org.cbioportal.application.security.token.oauth2.OAuth2TokenRefreshRestTemplate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.web.client.RestTemplate;

/**
 * {@code authenticate=saml} with {@code dat.method=oauth2}: SAML login through Keycloak and
 * Keycloak offline tokens as data access tokens, without basic login.
 */
@SpringJUnitWebConfig(
    classes = {
      Saml2SecurityConfig.class,
      ApiSecurityConfig.class,
      MethodSecurityConfig.class,
      DataAccessTokenConfig.class,
      SecurityChainTestSupport.ApiBeans.class,
      SecurityChainTestSupport.SamlIdpBeans.class
    })
@TestPropertySource(
    properties = {
      "authenticate=saml",
      "authorization=false",
      "filter_groups_by_appname=false",
      "security.cors.allowed-origins=*",
      "dat.method=oauth2",
      "dat.unauth_users=anonymousUser",
      "dat.oauth2.issuer=https://idp.example.org/realms/cbio",
      "dat.oauth2.clientId=cbioportal-api",
      "dat.oauth2.clientSecret=secret",
      "dat.oauth2.accessTokenUri=https://idp.example.org/token",
      "dat.oauth2.userAuthorizationUri=https://idp.example.org/auth",
      "dat.oauth2.jwkUrl=https://idp.example.org/certs",
      "dat.oauth2.redirectUri=http://localhost/api/data-access-token/oauth2",
      "dat.oauth2.jwtRolesPath=realm_access::roles"
    })
class SamlWithOAuth2TokensSecurityTest extends SecurityChainTestSupport {

  private static final String OFFLINE_TOKEN = "offline-token";

  @MockitoBean private OAuth2TokenRefreshRestTemplate tokenRefreshRestTemplate;
  @MockitoBean private JwtTokenVerifierBuilder jwtTokenVerifierBuilder;
  @MockitoBean private RestTemplate restTemplate;

  @Test
  void apiWithoutLoginIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/studies/" + PUBLIC_STUDY)).andExpect(status().isUnauthorized());
  }

  @Test
  void pageWithoutLoginRedirectsToSamlLogin() throws Exception {
    mockMvc
        .perform(get("/results").accept(MediaType.TEXT_HTML))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", containsString("/saml2/authenticate")));
  }

  @Test
  void samlUserReadsStudiesOfTheirGroupsOnly() throws Exception {
    mockMvc
        .perform(get("/api/studies/" + TEAM_STUDY).with(samlUser("TEAM_A")))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/api/studies/" + PRIVATE_STUDY).with(samlUser("TEAM_A")))
        .andExpect(status().isForbidden());
  }

  @Test
  void samlLogoutRedirectsToIdentityProvider() throws Exception {
    mockMvc
        .perform(post("/logout").with(samlUser("TEAM_A")))
        .andExpect(header().string("Location", startsWith(SAML_IDP_SLO_URL)));
  }

  @Test
  void basicLoginIsNotAvailable() throws Exception {
    mockMvc
        .perform(
            post("/j_spring_security_check").param("j_username", "user").param("j_password", "x"))
        .andExpect(unauthenticated());
  }

  @Test
  void offlineTokenGivesAccessToStudiesOfItsRealmRoles() throws Exception {
    Mockito.when(tokenRefreshRestTemplate.getAccessToken(OFFLINE_TOKEN))
        .thenReturn(
            OAuth2TokenAuthenticationProviderTest.accessToken(
                "{\"sub\": \"token-user\", \"realm_access\": {\"roles\": [\"TEAM_A\"]}}"));

    mockMvc
        .perform(get("/api/studies/" + TEAM_STUDY).with(bearer(OFFLINE_TOKEN)))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/api/studies/" + PRIVATE_STUDY).with(bearer(OFFLINE_TOKEN)))
        .andExpect(status().isForbidden());
  }

  @Test
  void offlineTokenThatCannotBeRefreshedIsUnauthorized() throws Exception {
    Mockito.when(tokenRefreshRestTemplate.getAccessToken(OFFLINE_TOKEN))
        .thenThrow(new BadCredentialsException("refresh failed"));

    mockMvc
        .perform(get("/api/studies/" + TEAM_STUDY).with(bearer(OFFLINE_TOKEN)))
        .andExpect(status().isUnauthorized());
  }
}
