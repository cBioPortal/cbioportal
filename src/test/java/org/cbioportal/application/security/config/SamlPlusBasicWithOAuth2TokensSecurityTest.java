package org.cbioportal.application.security.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cbioportal.application.security.token.config.DataAccessTokenConfig;
import org.cbioportal.application.security.token.oauth2.JwtTokenVerifierBuilder;
import org.cbioportal.application.security.token.oauth2.OAuth2TokenAuthenticationProviderTest;
import org.cbioportal.application.security.token.oauth2.OAuth2TokenRefreshRestTemplate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.web.client.RestTemplate;

/**
 * {@code authenticate=saml_plus_basic} with {@code dat.method=oauth2}: SAML login through Keycloak
 * plus one basic-login user, and Keycloak offline tokens as data access tokens.
 */
@SpringJUnitWebConfig(
    classes = {
      Saml2SecurityConfig.class,
      Saml2AndBasicConfig.class,
      ApiSecurityConfig.class,
      MethodSecurityConfig.class,
      DataAccessTokenConfig.class,
      SecurityChainTestSupport.ApiBeans.class,
      SecurityChainTestSupport.SamlIdpBeans.class
    })
@TestPropertySource(
    properties = {
      "authenticate=saml_plus_basic",
      "authorization=false",
      "filter_groups_by_appname=false",
      "security.cors.allowed-origins=*",
      "basic.username=" + SamlPlusBasicWithOAuth2TokensSecurityTest.BASIC_USER,
      "basic.password=" + SamlPlusBasicWithOAuth2TokensSecurityTest.BASIC_PASSWORD_HASH,
      "basic.authorities=PUBLIC",
      "dat.method=oauth2",
      "dat.oauth2.issuer=https://idp.example.org/realms/cbio",
      "dat.oauth2.clientId=cbioportal-api",
      "dat.oauth2.clientSecret=secret",
      "dat.oauth2.accessTokenUri=https://idp.example.org/token",
      "dat.oauth2.userAuthorizationUri=https://idp.example.org/auth",
      "dat.oauth2.jwkUrl=https://idp.example.org/certs",
      "dat.oauth2.redirectUri=http://localhost/api/data-access-token/oauth2",
      "dat.oauth2.jwtRolesPath=realm_access::roles"
    })
class SamlPlusBasicWithOAuth2TokensSecurityTest extends SecurityChainTestSupport {

  static final String BASIC_USER = "basic_user";
  static final String BASIC_PASSWORD = "basic_password";
  static final String BASIC_PASSWORD_HASH =
      "$2a$10$gwP4PtbDn1KICEKTfS6AdOo6YsuQV5DXOaUGHTv/e3sYQRNlPH7Q2";
  private static final String OFFLINE_TOKEN = "offline-token";

  @MockitoBean private OAuth2TokenRefreshRestTemplate tokenRefreshRestTemplate;
  @MockitoBean private JwtTokenVerifierBuilder jwtTokenVerifierBuilder;
  @MockitoBean private RestTemplate restTemplate;

  // Unauthenticated requests

  @Test
  void healthIsPublic() throws Exception {
    mockMvc.perform(get("/api/health")).andExpect(status().isOk());
  }

  @Test
  void apiWithoutLoginIsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/api/legacy/studies/" + PUBLIC_STUDY))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void pageWithoutLoginRedirectsToSamlLogin() throws Exception {
    mockMvc
        .perform(get("/results").accept(MediaType.TEXT_HTML))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", containsString("/saml2/authenticate")));
  }

  @Test
  void corsPreflightOnApiIsAnswered() throws Exception {
    mockMvc
        .perform(
            options("/api/legacy/studies/" + PUBLIC_STUDY)
                .header("Origin", "https://frontend.example.org")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "*"));
  }

  // SAML login

  @Test
  void samlUserReadsStudiesOfTheirGroupsOnly() throws Exception {
    mockMvc
        .perform(get("/api/legacy/studies/" + TEAM_STUDY).with(samlUser("TEAM_A")))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/api/legacy/studies/" + PRIVATE_STUDY).with(samlUser("TEAM_A")))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(get("/api/legacy/studies/" + PUBLIC_STUDY).with(samlUser("TEAM_A")))
        .andExpect(status().isForbidden());
  }

  @Test
  void samlUserCannotFetchAMixOfReadableAndUnreadableStudies() throws Exception {
    mockMvc
        .perform(
            post("/api/studies/fetch")
                .with(samlUser("TEAM_A"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"" + TEAM_STUDY + "\", \"" + PRIVATE_STUDY + "\"]"))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(
            post("/api/studies/fetch")
                .with(samlUser("TEAM_A"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"" + TEAM_STUDY + "\"]"))
        .andExpect(status().isOk());
  }

  @Test
  void samlLogoutRedirectsToIdentityProvider() throws Exception {
    mockMvc
        .perform(post("/logout").with(samlUser("TEAM_A")))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", startsWith(SAML_IDP_SLO_URL)))
        .andExpect(header().string("Location", containsString("SAMLRequest=")));
  }

  // Basic login

  @Test
  void basicLoginGivesAccessToStudiesOfConfiguredAuthorities() throws Exception {
    MockHttpSession session =
        (MockHttpSession)
            mockMvc
                .perform(basicLogin(BASIC_PASSWORD))
                .andExpect(authenticated().withUsername(BASIC_USER))
                .andReturn()
                .getRequest()
                .getSession();

    mockMvc
        .perform(get("/api/legacy/studies/" + PUBLIC_STUDY).session(session))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/api/legacy/studies/" + PRIVATE_STUDY).session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void basicLoginWithWrongPasswordFails() throws Exception {
    mockMvc.perform(basicLogin("wrong_password")).andExpect(unauthenticated());
  }

  @Test
  void basicLogoutRedirectsHome() throws Exception {
    MockHttpSession session =
        (MockHttpSession)
            mockMvc.perform(basicLogin(BASIC_PASSWORD)).andReturn().getRequest().getSession();

    mockMvc
        .perform(get("/j_spring_security_logout").session(session))
        .andExpect(redirectedUrl("/"));
    mockMvc
        .perform(get("/api/legacy/studies/" + PUBLIC_STUDY).session(session))
        .andExpect(status().isUnauthorized());
  }

  // Data access tokens (Keycloak offline tokens)

  @Test
  void offlineTokenGivesAccessToStudiesOfItsRealmRoles() throws Exception {
    Mockito.when(tokenRefreshRestTemplate.getAccessToken(OFFLINE_TOKEN))
        .thenReturn(
            OAuth2TokenAuthenticationProviderTest.accessToken(
                "{\"sub\": \"token-user\", \"realm_access\": {\"roles\": [\"TEAM_A\"]}}"));

    mockMvc
        .perform(get("/api/legacy/studies/" + TEAM_STUDY).with(bearer(OFFLINE_TOKEN)))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/api/legacy/studies/" + PRIVATE_STUDY).with(bearer(OFFLINE_TOKEN)))
        .andExpect(status().isForbidden());
  }

  @Test
  void offlineTokenThatCannotBeRefreshedIsUnauthorized() throws Exception {
    Mockito.when(tokenRefreshRestTemplate.getAccessToken(OFFLINE_TOKEN))
        .thenThrow(new BadCredentialsException("refresh failed"));

    mockMvc
        .perform(get("/api/legacy/studies/" + TEAM_STUDY).with(bearer(OFFLINE_TOKEN)))
        .andExpect(status().isUnauthorized());
  }

  private static org.springframework.test.web.servlet.RequestBuilder basicLogin(String password) {
    return post("/j_spring_security_check")
        .param("j_username", BASIC_USER)
        .param("j_password", password);
  }
}
