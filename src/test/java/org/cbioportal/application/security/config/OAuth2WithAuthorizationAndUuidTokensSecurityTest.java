package org.cbioportal.application.security.config;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;
import org.cbioportal.application.security.token.config.DataAccessTokenConfig;
import org.cbioportal.legacy.model.DataAccessToken;
import org.cbioportal.legacy.model.UserAuthorities;
import org.cbioportal.legacy.persistence.DataAccessTokenRepository;
import org.cbioportal.legacy.persistence.SecurityRepository;
import org.cbioportal.legacy.service.impl.UuidDataAccessTokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

/**
 * {@code authenticate=oauth2} with {@code authorization=true} and {@code dat.method=uuid}: required
 * OIDC login through Google, study access from the portal database and UUID data access tokens.
 * {@code filter_groups_by_appname} is left unset, so authorities need the app-name prefix.
 */
@SpringJUnitWebConfig(
    classes = {
      OAuth2SecurityConfig.class,
      CustomOAuth2AuthorizationConfig.class,
      ApiSecurityConfig.class,
      MethodSecurityConfig.class,
      DataAccessTokenConfig.class,
      UuidDataAccessTokenServiceImpl.class,
      SecurityChainTestSupport.ApiBeans.class,
      SecurityChainTestSupport.OidcIdpBeans.class
    })
@TestPropertySource(
    properties = {
      "authenticate=oauth2",
      "authorization=true",
      "app.name=" + OAuth2WithAuthorizationAndUuidTokensSecurityTest.APP_NAME,
      "security.force_redirect_on_one_idp=false",
      "security.cors.allowed-origins=*",
      "dat.method=uuid",
      "dat.uuid.max_number_per_user=1",
      "dat.ttl_seconds=2592000",
      "dat.unauth_users=anonymousUser"
    })
class OAuth2WithAuthorizationAndUuidTokensSecurityTest extends SecurityChainTestSupport {

  static final String APP_NAME = "test-portal";
  private static final String TEAM_A = APP_NAME + ":TEAM_A";
  private static final String VALID_TOKEN = "11111111-1111-1111-1111-111111111111";
  private static final String EXPIRED_TOKEN = "22222222-2222-2222-2222-222222222222";
  private static final String UNKNOWN_TOKEN = "33333333-3333-3333-3333-333333333333";

  @MockitoBean private DataAccessTokenRepository dataAccessTokenRepository;
  @Autowired private SecurityRepository securityRepository;
  @Autowired private ClientRegistrationRepository registrations;

  // Unauthenticated requests

  @Test
  void healthIsPublic() throws Exception {
    mockMvc.perform(get("/api/health")).andExpect(status().isOk());
  }

  @Test
  void apiWithoutLoginIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/legacy/studies/" + TEAM_STUDY)).andExpect(status().isUnauthorized());
  }

  @Test
  void pageWithoutLoginRedirectsToLoginPage() throws Exception {
    mockMvc
        .perform(get("/results").accept(MediaType.TEXT_HTML))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  @Test
  void corsPreflightOnApiIsAnswered() throws Exception {
    mockMvc
        .perform(
            options("/api/legacy/studies/" + TEAM_STUDY)
                .header("Origin", "https://frontend.example.org")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "*"));
  }

  // OIDC login

  @Test
  void userReadsStudiesOfTheirGroupsForThisPortal() throws Exception {
    mockMvc
        .perform(get("/api/legacy/studies/" + TEAM_STUDY).with(googleUser(TEAM_A)))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/api/legacy/studies/" + PRIVATE_STUDY).with(googleUser(TEAM_A)))
        .andExpect(status().isForbidden());
  }

  @Test
  void groupsWithoutThisPortalsPrefixDoNotCount() throws Exception {
    mockMvc
        .perform(get("/api/legacy/studies/" + TEAM_STUDY).with(googleUser("TEAM_A")))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(get("/api/legacy/studies/" + TEAM_STUDY).with(googleUser("other-portal:TEAM_A")))
        .andExpect(status().isForbidden());
  }

  @Test
  void logoutFromProviderWithoutEndSessionEndpointLandsOnLoginPage() throws Exception {
    mockMvc
        .perform(post("/logout").with(googleUser(TEAM_A)))
        .andExpect(redirectedUrl("/login?logout_success"));
  }

  @Test
  void logoutFromProviderWithEndSessionEndpointRedirectsThere() throws Exception {
    mockMvc
        .perform(post("/logout").with(oidcUser(registrations, KEYCLOAK_REGISTRATION_ID, TEAM_A)))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", startsWith(OIDC_END_SESSION_URL)))
        .andExpect(header().string("Location", containsString("post_logout_redirect_uri=")));
  }

  // UUID data access tokens

  @Test
  void validTokenGivesAccessToStudiesOfTheOwnersDatabaseGroups() throws Exception {
    givenToken(VALID_TOKEN, "token-user@example.org", 1);
    Mockito.when(securityRepository.getPortalUserAuthorities("token-user@example.org"))
        .thenReturn(new UserAuthorities("token-user@example.org", List.of(TEAM_A)));

    mockMvc
        .perform(get("/api/legacy/studies/" + TEAM_STUDY).with(bearer(VALID_TOKEN)))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/api/legacy/studies/" + PRIVATE_STUDY).with(bearer(VALID_TOKEN)))
        .andExpect(status().isForbidden());
  }

  @Test
  void expiredTokenIsUnauthorized() throws Exception {
    givenToken(EXPIRED_TOKEN, "token-user@example.org", -1);

    mockMvc
        .perform(get("/api/legacy/studies/" + TEAM_STUDY).with(bearer(EXPIRED_TOKEN)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void unknownTokenIsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/api/legacy/studies/" + TEAM_STUDY).with(bearer(UNKNOWN_TOKEN)))
        .andExpect(status().isUnauthorized());
  }

  private org.springframework.test.web.servlet.request.RequestPostProcessor googleUser(
      String... authorities) {
    return oidcUser(registrations, GOOGLE_REGISTRATION_ID, authorities);
  }

  private void givenToken(String token, String username, int expiresInDays) {
    Date expiration = new Date(System.currentTimeMillis() + expiresInDays * 86_400_000L);
    Mockito.when(dataAccessTokenRepository.getDataAccessToken(token))
        .thenReturn(new DataAccessToken(token, username, expiration, new Date()));
  }
}
