package org.cbioportal.application.security.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

/**
 * Optional login with study authorization turned on through {@code
 * security.method_authorization_enabled}: anonymous users read only the always-shown group.
 */
@SpringJUnitWebConfig(
    classes = {
      OptionalOAuth2SecurityConfig.class,
      MethodSecurityConfig.class,
      SecurityChainTestSupport.ApiBeans.class,
      SecurityChainTestSupport.OidcIdpBeans.class
    })
@TestPropertySource(
    properties = {
      "authenticate=optional_oauth2",
      "security.method_authorization_enabled=true",
      "always_show_study_group=PUBLIC",
      "app.name=test-portal",
      "security.cors.allowed-origins=*"
    })
class OptionalOAuth2WithStudyAuthorizationSecurityTest extends SecurityChainTestSupport {

  @Autowired private ClientRegistrationRepository registrations;

  @Test
  void anonymousUserReadsAlwaysShownStudiesOnly() throws Exception {
    mockMvc.perform(get("/api/studies/" + PUBLIC_STUDY)).andExpect(status().isOk());
    // Denied anonymous requests go to the login entry point, as there is no API chain
    mockMvc.perform(get("/api/studies/" + TEAM_STUDY)).andExpect(redirectedUrl("/login"));
  }

  @Test
  void signedInUserAlsoReadsStudiesOfTheirGroups() throws Exception {
    var user = oidcUser(registrations, GOOGLE_REGISTRATION_ID, "test-portal:TEAM_A");
    mockMvc.perform(get("/api/studies/" + PUBLIC_STUDY).with(user)).andExpect(status().isOk());
    mockMvc.perform(get("/api/studies/" + TEAM_STUDY).with(user)).andExpect(status().isOk());
    mockMvc
        .perform(get("/api/studies/" + PRIVATE_STUDY).with(user))
        .andExpect(status().isForbidden());
  }
}
