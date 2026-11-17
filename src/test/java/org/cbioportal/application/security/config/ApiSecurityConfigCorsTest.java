package org.cbioportal.application.security.config;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** CORS preflight requests to the API filter chain, which is active in every login mode. */
@SpringJUnitWebConfig(
    classes = {
      ApiSecurityConfig.class,
      CorsConfig.class,
      ApiSecurityConfigCorsTest.WebSecurity.class
    })
@TestPropertySource(
    properties = {
      "authenticate=saml",
      "security.cors.allowed-origins=" + ApiSecurityConfigCorsTest.ALLOWED_ORIGIN,
      "security.cors.allow-credentials=true"
    })
class ApiSecurityConfigCorsTest {

  static final String ALLOWED_ORIGIN = "https://frontend.example.org";

  @Configuration
  @EnableWebSecurity
  static class WebSecurity {}

  @Autowired private WebApplicationContext context;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  void preflightFromAllowedOriginIsAnsweredWithoutLogin() throws Exception {
    mockMvc
        .perform(
            options("/api/studies")
                .header("Origin", ALLOWED_ORIGIN)
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
        .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
  }

  @Test
  void preflightFromOtherOriginIsRejected() throws Exception {
    mockMvc
        .perform(
            options("/api/studies")
                .header("Origin", "https://elsewhere.example.org")
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isForbidden());
  }
}
