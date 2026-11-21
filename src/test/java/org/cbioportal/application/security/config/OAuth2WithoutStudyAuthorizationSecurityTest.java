package org.cbioportal.application.security.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cbioportal.application.security.token.config.DataAccessTokenConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

/**
 * {@code authenticate=oauth2} with method security off: login required through Google or Microsoft,
 * no study authorization beyond the always-shown group, no data access tokens.
 */
@SpringJUnitWebConfig(
    classes = {
      OAuth2SecurityConfig.class,
      ApiSecurityConfig.class,
      MethodSecurityConfig.class,
      DataAccessTokenConfig.class,
      SecurityChainTestSupport.ApiBeans.class,
      SecurityChainTestSupport.OidcIdpBeans.class
    })
@TestPropertySource(
    properties = {
      "authenticate=oauth2",
      "authorization=false",
      "security.method_authorization_enabled=false",
      "always_show_study_group=PUBLIC",
      "app.name=test-portal",
      "dat.method=none",
      "security.cors.allowed-origins=*"
    })
class OAuth2WithoutStudyAuthorizationSecurityTest extends SecurityChainTestSupport {

  @Autowired private ClientRegistrationRepository registrations;

  @Test
  void apiWithoutLoginIsUnauthorizedEvenForPublicStudies() throws Exception {
    mockMvc.perform(get("/api/studies/" + PUBLIC_STUDY)).andExpect(status().isUnauthorized());
  }

  @Test
  void pageWithoutLoginRedirectsToLoginPage() throws Exception {
    mockMvc.perform(get("/results").accept(MediaType.TEXT_HTML)).andExpect(redirectedUrl("/login"));
  }

  @Test
  void signedInUserWithoutRoleClaimsReadsAlwaysShownStudiesOnly() throws Exception {
    var user = oidcUser(registrations, GOOGLE_REGISTRATION_ID);
    mockMvc.perform(get("/api/studies/" + PUBLIC_STUDY).with(user)).andExpect(status().isOk());
    mockMvc.perform(get("/api/studies/" + TEAM_STUDY).with(user)).andExpect(status().isForbidden());
  }

  @Test
  void bearerTokensAreNotAccepted() throws Exception {
    mockMvc
        .perform(get("/api/studies/" + PUBLIC_STUDY).with(bearer("any-token")))
        .andExpect(status().isUnauthorized());
  }
}
