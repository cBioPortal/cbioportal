package org.cbioportal.application.security.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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
  void keepsCredentialsOffUnlessEnabled() {
    CorsConfiguration configuration =
        corsConfigurationSource("https://portal.example.org", false)
            .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/studies"));

    assertNotNull(configuration);
    assertFalse(Boolean.TRUE.equals(configuration.getAllowCredentials()));
  }

  @Test
  void disablesCredentialsForTheWildcardOrigin() {
    CorsConfiguration configuration =
        corsConfigurationSource("*", true)
            .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/studies"));

    assertNotNull(configuration);
    assertFalse(configuration.getAllowCredentials());
  }

  @Test
  void registersNothingWithoutAllowedOrigins() {
    for (String unset : new String[] {"", " ", ", ,"}) {
      assertNull(
          corsConfigurationSource(unset, true)
              .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/studies")),
          "allowed-origins='" + unset + "'");
    }
  }

  @Test
  void trimsTheAllowedOrigins() {
    CorsConfiguration configuration =
        corsConfigurationSource(" https://portal.example.org , https://preview.example.org ", true)
            .getCorsConfiguration(new MockHttpServletRequest("GET", "/api/studies"));

    assertNotNull(configuration);
    assertEquals(
        List.of("https://portal.example.org", "https://preview.example.org"),
        configuration.getAllowedOrigins());
  }

  private CorsConfigurationSource corsConfigurationSource() {
    return corsConfigurationSource(
        "https://portal.example.org,https://deploy-preview.example.org", true);
  }

  private CorsConfigurationSource corsConfigurationSource(
      String allowedOrigins, boolean allowCredentials) {
    CorsConfig config = new CorsConfig();
    ReflectionTestUtils.setField(config, "allowedOrigins", allowedOrigins);
    ReflectionTestUtils.setField(config, "allowCredentials", allowCredentials);
    return config.corsConfigurationSource();
  }
}
