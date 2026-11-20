package org.cbioportal.application.security.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cbioportal.application.security.token.config.DataAccessTokenConfig;
import org.cbioportal.application.security.token.oauth2.JwtTokenVerifierBuilder;
import org.cbioportal.application.security.token.oauth2.OAuth2TokenRefreshRestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.web.client.RestTemplate;

/**
 * {@code authenticate=saml} with {@code authorization=true}, which applies to OIDC logins only, so
 * SAML roles still decide study access.
 *
 * <p>A reverse proxy can authorize requests to study resources with a subrequest to {@code
 * /api/studies/{studyId}} carrying the browser's session, reading 2xx as allowed and 401/403 as
 * denied.
 */
@SpringJUnitWebConfig(
    classes = {
      Saml2SecurityConfig.class,
      CustomOAuth2AuthorizationConfig.class,
      ApiSecurityConfig.class,
      MethodSecurityConfig.class,
      DataAccessTokenConfig.class,
      SecurityChainTestSupport.ApiBeans.class,
      SecurityChainTestSupport.SamlIdpBeans.class
    })
@TestPropertySource(
    properties = {
      "authenticate=saml",
      "authorization=true",
      "filter_groups_by_appname=false",
      "security.cors.allowed-origins=*",
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
class SamlWithAuthorizationSecurityTest extends SecurityChainTestSupport {

  @MockitoBean private OAuth2TokenRefreshRestTemplate tokenRefreshRestTemplate;
  @MockitoBean private JwtTokenVerifierBuilder jwtTokenVerifierBuilder;
  @MockitoBean private RestTemplate restTemplate;

  @Test
  void studyCheckAllowsSignedInUserWithAccess() throws Exception {
    mockMvc
        .perform(get("/api/studies/" + TEAM_STUDY).with(samlUser("TEAM_A")))
        .andExpect(status().isOk());
  }

  @Test
  void studyCheckDeniesSignedInUserWithoutAccess() throws Exception {
    mockMvc
        .perform(get("/api/studies/" + PRIVATE_STUDY).with(samlUser("TEAM_A")))
        .andExpect(status().isForbidden());
  }

  @Test
  void studyCheckDeniesRequestWithoutSession() throws Exception {
    mockMvc.perform(get("/api/studies/" + TEAM_STUDY)).andExpect(status().isUnauthorized());
  }
}
