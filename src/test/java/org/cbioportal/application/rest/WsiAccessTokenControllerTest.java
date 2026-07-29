package org.cbioportal.application.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.web.config.TestConfig;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(
    classes = {
      WsiAccessTokenController.class,
      TestConfig.class,
      WsiAccessTokenControllerTest.MethodSecurityTestConfig.class
    })
public class WsiAccessTokenControllerTest {

  @TestConfiguration
  @EnableMethodSecurity(prePostEnabled = true)
  static class MethodSecurityTestConfig {

    @Bean
    MethodSecurityExpressionHandler methodSecurityExpressionHandler(
        CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator) {
      DefaultMethodSecurityExpressionHandler expressionHandler =
          new DefaultMethodSecurityExpressionHandler();
      expressionHandler.setPermissionEvaluator(cancerStudyPermissionEvaluator);
      return expressionHandler;
    }
  }

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator;

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void returnsUnauthorizedWithoutAuthentication() throws Exception {
    mockMvc
        .perform(get("/api/wsi/access-token").param("studyId", "msk_spectrum_tme_2022"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  public void returnsForbiddenWhenPermissionIsDenied() throws Exception {
    when(cancerStudyPermissionEvaluator.hasPermission(
            any(Authentication.class),
            eq("msk_spectrum_tme_2022"),
            eq("CancerStudyId"),
            eq(AccessLevel.READ)))
        .thenReturn(false);

    mockMvc
        .perform(get("/api/wsi/access-token").param("studyId", "msk_spectrum_tme_2022"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser
  public void returnsBadRequestWhenStudyIdIsBlank() throws Exception {
    when(cancerStudyPermissionEvaluator.hasPermission(
            any(Authentication.class), eq(" "), eq("CancerStudyId"), eq(AccessLevel.READ)))
        .thenReturn(true);

    mockMvc.perform(get("/api/wsi/access-token").param("studyId", " ")).andExpect(status().isBadRequest());
  }

  @Test
  public void returnsServiceUnavailableWhenSecretIsTooShort() {
    WsiAccessTokenController plainController = createAuthenticatedController();
    ReflectionTestUtils.setField(plainController, "accessTokenSecret", "short-secret");
    ReflectionTestUtils.setField(
        plainController, "cancerStudyPermissionEvaluator", cancerStudyPermissionEvaluator);
    when(cancerStudyPermissionEvaluator.hasPermission(
            any(Authentication.class),
            eq("msk_spectrum_tme_2022"),
            eq("CancerStudyId"),
            eq(AccessLevel.READ)))
        .thenReturn(true);

    ResponseEntity<?> response = plainController.issueAccessToken("msk_spectrum_tme_2022");

    assertEquals(503, response.getStatusCode().value());
  }

  @Test
  public void returnsServiceUnavailableWhenPermissionEvaluatorIsMissing() {
    WsiAccessTokenController plainController = createAuthenticatedController();

    ResponseEntity<?> response = plainController.issueAccessToken("msk_spectrum_tme_2022");

    assertEquals(503, response.getStatusCode().value());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void returnsSignedTokenWhenAuthorized() {
    WsiAccessTokenController plainController = createAuthenticatedController();
    ReflectionTestUtils.setField(
        plainController, "accessTokenSecret", "0123456789abcdef0123456789abcdef");
    ReflectionTestUtils.setField(plainController, "accessTokenAudience", "cbioportal-wsi");
    ReflectionTestUtils.setField(plainController, "accessTokenTtlSeconds", 300);
    ReflectionTestUtils.setField(
        plainController, "cancerStudyPermissionEvaluator", cancerStudyPermissionEvaluator);
    when(cancerStudyPermissionEvaluator.hasPermission(
            any(Authentication.class),
            eq("msk_spectrum_tme_2022"),
            eq("CancerStudyId"),
            eq(AccessLevel.READ)))
        .thenReturn(true);

    ResponseEntity<?> response = plainController.issueAccessToken("msk_spectrum_tme_2022");

    assertEquals(200, response.getStatusCode().value());
    Map<String, Object> body = (Map<String, Object>) response.getBody();
    assertNotNull(body);
    assertEquals("Bearer", body.get("token_type"));
    assertEquals(300, body.get("expires_in"));
    assertTrue(((String) body.get("access_token")).length() > 20);
  }

  private WsiAccessTokenController createAuthenticatedController() {
    WsiAccessTokenController plainController = new WsiAccessTokenController();
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken("user", "password", "ROLE_USER");
    authentication.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    return plainController;
  }
}
