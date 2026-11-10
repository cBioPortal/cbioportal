/*
 * Copyright (c) 2026 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.application.security.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.cbioportal.legacy.service.FrontendPropertiesService;
import org.cbioportal.legacy.web.LoginPageController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * After SAML single logout the IdP returns the browser to {@code /login?logout_success}. These
 * tests drive the real SAML filter chain to check that the signed-out page renders and that an
 * error page is never saved as the post-login redirect. Subclasses pick the {@code authenticate}
 * mode.
 */
@WebMvcTest
@ContextConfiguration(
    classes = {
      Saml2SecurityConfig.class,
      Saml2AndBasicConfig.class,
      LoginPageController.class,
      AbstractSamlLoginPageSecurityTest.RelyingPartyConfig.class
    })
abstract class AbstractSamlLoginPageSecurityTest {

  private static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";

  private static final String BROWSER_ACCEPT =
      "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FrontendPropertiesService frontendPropertiesService;

  @BeforeEach
  void stubFrontendProperties() {
    when(frontendPropertiesService.getFrontendProperty(any())).thenReturn("");
  }

  @Test
  void logoutSuccessShowsTheSignedOutPage() throws Exception {
    mockMvc
        .perform(get("/login?logout_success").header("Accept", BROWSER_ACCEPT))
        .andExpect(status().isOk())
        .andExpect(view().name("login"))
        .andExpect(model().attribute("logout_success", true));
  }

  @Test
  void errorPageIsNotSavedAsThePostLoginRedirect() throws Exception {
    mockMvc
        .perform(get("/error?logout_success").header("Accept", BROWSER_ACCEPT))
        .andExpect(result -> assertFalse(result.getResponse().getStatus() / 100 == 3, "redirected"))
        .andExpect(request().sessionAttributeDoesNotExist(SAVED_REQUEST));
  }

  @Test
  void protectedPagesStillRedirectToTheIdentityProvider() throws Exception {
    mockMvc
        .perform(get("/results").header("Accept", BROWSER_ACCEPT))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/saml2/authenticate?registrationId=cbio-saml-idp"));
  }

  @Configuration
  static class RelyingPartyConfig {

    @Bean
    RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
      return new InMemoryRelyingPartyRegistrationRepository(
          RelyingPartyRegistration.withRegistrationId("cbio-saml-idp")
              .entityId("cbioportal-test")
              .assertingPartyMetadata(
                  party ->
                      party
                          .entityId("https://idp.example.org")
                          .singleSignOnServiceLocation("https://idp.example.org/sso"))
              .build());
    }
  }
}
