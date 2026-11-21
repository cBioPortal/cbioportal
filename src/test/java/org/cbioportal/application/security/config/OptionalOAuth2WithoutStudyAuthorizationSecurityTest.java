package org.cbioportal.application.security.config;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
 * {@code authenticate=optional_oauth2} with method security off: optional login through Google or
 * Microsoft, no study authorization, no data access tokens. The API chain, method security and
 * token configuration are listed so the test shows they stay inactive.
 */
@SpringJUnitWebConfig(
    classes = {
      OptionalOAuth2SecurityConfig.class,
      ApiSecurityConfig.class,
      MethodSecurityConfig.class,
      DataAccessTokenConfig.class,
      SecurityChainTestSupport.ApiBeans.class,
      SecurityChainTestSupport.OidcIdpBeans.class
    })
@TestPropertySource(
    properties = {
      "authenticate=optional_oauth2",
      "authorization=false",
      "security.method_authorization_enabled=false",
      "always_show_study_group=PUBLIC",
      "app.name=test-portal",
      "dat.method=none",
      "security.cors.allowed-origins=*"
    })
class OptionalOAuth2WithoutStudyAuthorizationSecurityTest extends SecurityChainTestSupport {

  @Autowired private ClientRegistrationRepository registrations;

  @Test
  void anonymousUserReadsEveryStudy() throws Exception {
    mockMvc.perform(get("/api/studies/" + PRIVATE_STUDY)).andExpect(status().isOk());
    mockMvc.perform(get("/api/legacy/studies/" + TEAM_STUDY)).andExpect(status().isOk());
  }

  @Test
  void pagesDoNotRequireLogin() throws Exception {
    mockMvc.perform(get("/results").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound());
  }

  @Test
  void loginStartsAtTheChosenProvider() throws Exception {
    mockMvc
        .perform(get("/oauth2/authorization/" + GOOGLE_REGISTRATION_ID))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", startsWith("https://google.example.org/auth")));
  }

  @Test
  void logoutReturnsToHomePage() throws Exception {
    mockMvc
        .perform(post("/logout").with(oidcUser(registrations, GOOGLE_REGISTRATION_ID)))
        .andExpect(redirectedUrl("/"));
  }

  @Test
  void bearerTokensAreIgnored() throws Exception {
    mockMvc
        .perform(get("/api/studies/" + PRIVATE_STUDY).with(bearer("any-token")))
        .andExpect(status().isOk())
        .andExpect(unauthenticated());
  }

  @Test
  void corsPreflightOnApiIsAnswered() throws Exception {
    mockMvc
        .perform(
            options("/api/studies/" + PUBLIC_STUDY)
                .header("Origin", "https://frontend.example.org")
                .header("Access-Control-Request-Method", "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "*"));
  }
}
