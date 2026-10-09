package org.cbioportal.application.security.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class CorsConfigTest {

  @Test
  void skipsCorsProcessingForSamlResponsePosts() {
    CorsConfigurationSource source = corsConfigurationSource();
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/login/saml2/sso/cbio-saml-idp");

    assertNull(source.getCorsConfiguration(request));
  }

  @Test
  void skipsCorsProcessingForSamlResponsePostsWithAContextPath() {
    CorsConfigurationSource source = corsConfigurationSource();
    MockHttpServletRequest request =
        new MockHttpServletRequest("POST", "/portal/login/saml2/sso/cbio-saml-idp");
    request.setContextPath("/portal");

    assertNull(source.getCorsConfiguration(request));
  }

  @Test
  void retainsCorsProcessingForApiRequests() {
    CorsConfigurationSource source = corsConfigurationSource();
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/studies");

    CorsConfiguration configuration = source.getCorsConfiguration(request);

    assertNotNull(configuration);
    assertTrue(configuration.getAllowedHeaders().contains("Authorization"));
    assertTrue(configuration.getAllowCredentials());
  }

  @Test
  void disablesCredentialsForTheWildcardOrigin() {
    CorsConfig config = new CorsConfig();
    ReflectionTestUtils.setField(config, "allowedOrigins", "*");
    CorsConfiguration configuration =
        config
            .corsConfigurationSource()
            .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/studies"));

    assertNotNull(configuration);
    assertFalse(configuration.getAllowCredentials());
  }

  private CorsConfigurationSource corsConfigurationSource() {
    CorsConfig config = new CorsConfig();
    ReflectionTestUtils.setField(
        config, "allowedOrigins", "https://portal.example.org,https://deploy-preview.example.org");
    return config.corsConfigurationSource();
  }
}
