package org.cbioportal.application.security.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

/** With {@code authenticate=false} every request is allowed and no login configuration loads. */
@SpringJUnitWebConfig(
    classes = {
      NoSecurityConfig.class,
      ApiSecurityConfig.class,
      MethodSecurityConfig.class,
      OptionalOAuth2SecurityConfig.class,
      OAuth2SecurityConfig.class,
      Saml2SecurityConfig.class,
      Saml2AndBasicConfig.class,
      SecurityChainTestSupport.ApiBeans.class
    })
@TestPropertySource(properties = {"authenticate=false", "security.cors.allowed-origins=*"})
class NoAuthenticationSecurityTest extends SecurityChainTestSupport {

  @Autowired private ApplicationContext context;

  @Test
  void onlyTheOpenFilterChainLoads() {
    assertEquals(1, context.getBeanNamesForType(NoSecurityConfig.class).length);
    assertEquals(0, context.getBeanNamesForType(ApiSecurityConfig.class).length);
    assertEquals(0, context.getBeanNamesForType(CancerStudyPermissionEvaluator.class).length);
  }

  @Test
  void everyStudyIsReadableWithoutLogin() throws Exception {
    mockMvc.perform(get("/api/studies/" + PRIVATE_STUDY)).andExpect(status().isOk());
    mockMvc.perform(get("/api/legacy/studies/" + PRIVATE_STUDY)).andExpect(status().isOk());
  }

  @Test
  void pagesDoNotRequireLogin() throws Exception {
    mockMvc.perform(get("/results").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound());
  }
}
