package org.cbioportal.application.security.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cbioportal.application.security.token.config.DataAccessTokenConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

/**
 * {@code authenticate=saml} with {@code dat.method} unset: SAML login through Keycloak and no data
 * access tokens.
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
      "security.cors.allowed-origins=*"
    })
class SamlWithoutTokensSecurityTest extends SecurityChainTestSupport {

  @Test
  void apiWithoutLoginIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/studies/" + PUBLIC_STUDY)).andExpect(status().isUnauthorized());
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
  void bearerTokensAreNotAccepted() throws Exception {
    mockMvc
        .perform(get("/api/studies/" + PUBLIC_STUDY).with(bearer("any-token")))
        .andExpect(status().isUnauthorized());
  }
}
