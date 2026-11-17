package org.cbioportal.application.security.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;
import org.cbioportal.application.security.token.config.DataAccessTokenConfig;
import org.cbioportal.legacy.model.DataAccessToken;
import org.cbioportal.legacy.model.UserAuthorities;
import org.cbioportal.legacy.persistence.DataAccessTokenRepository;
import org.cbioportal.legacy.persistence.SecurityRepository;
import org.cbioportal.legacy.service.impl.UuidDataAccessTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Basic login and UUID data access tokens under {@code authenticate=saml_plus_basic}, through the
 * production filter chains.
 */
@SpringJUnitWebConfig(
    classes = {
      Saml2SecurityConfig.class,
      Saml2AndBasicConfig.class,
      ApiSecurityConfig.class,
      DataAccessTokenConfig.class,
      UuidDataAccessTokenServiceImpl.class,
      CorsConfig.class,
      Saml2AndBasicConfigTest.TestBeans.class
    })
@TestPropertySource(
    properties = {
      "authenticate=saml_plus_basic",
      "dat.method=uuid",
      "basic.username=" + Saml2AndBasicConfigTest.BASIC_USER,
      "basic.password=" + Saml2AndBasicConfigTest.BASIC_PASSWORD_HASH,
      "basic.authorities=PUBLIC"
    })
class Saml2AndBasicConfigTest {

  static final String BASIC_USER = "basic_user";
  static final String BASIC_PASSWORD = "basic_password";
  static final String BASIC_PASSWORD_HASH =
      "$2a$10$gwP4PtbDn1KICEKTfS6AdOo6YsuQV5DXOaUGHTv/e3sYQRNlPH7Q2";

  private static final String TOKEN_USER = "token_user";
  private static final String VALID_TOKEN = "11111111-1111-1111-1111-111111111111";
  private static final String UNKNOWN_TOKEN = "22222222-2222-2222-2222-222222222222";

  @Configuration
  static class TestBeans {
    @Bean
    SecurityRepository securityRepository() {
      return Mockito.mock(SecurityRepository.class);
    }

    @Bean
    DataAccessTokenRepository dataAccessTokenRepository() {
      return Mockito.mock(DataAccessTokenRepository.class);
    }

    @Bean
    RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
      return new InMemoryRelyingPartyRegistrationRepository(
          RelyingPartyRegistration.withRegistrationId("keycloak")
              .entityId("cbioportal")
              .assertingPartyMetadata(
                  idp ->
                      idp.entityId("https://idp.example.org")
                          .singleSignOnServiceLocation("https://idp.example.org/sso"))
              .build());
    }
  }

  @Autowired private WebApplicationContext context;
  @Autowired private SecurityRepository securityRepository;
  @Autowired private DataAccessTokenRepository dataAccessTokenRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    // springSecurity() copies this context into each request; other tests leave mock users in it
    TestSecurityContextHolder.clearContext();
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    Mockito.when(securityRepository.getPortalUserAuthorities(Mockito.anyString()))
        .thenAnswer(
            invocation -> new UserAuthorities(invocation.getArgument(0), List.of("PRIVATE_GROUP")));
    Date tomorrow = new Date(System.currentTimeMillis() + 86_400_000L);
    Mockito.when(dataAccessTokenRepository.getDataAccessToken(VALID_TOKEN))
        .thenReturn(new DataAccessToken(VALID_TOKEN, TOKEN_USER, tomorrow, new Date()));
  }

  @Test
  void passwordHashMatchesPassword() {
    assertTrue(new BCryptPasswordEncoder().matches(BASIC_PASSWORD, BASIC_PASSWORD_HASH));
  }

  @Test
  void loginWithCorrectPasswordSucceeds() throws Exception {
    mockMvc
        .perform(login(BASIC_USER, BASIC_PASSWORD))
        .andExpect(authenticated().withUsername(BASIC_USER));
  }

  @Test
  void loginWithWrongPasswordFails() throws Exception {
    mockMvc.perform(login(BASIC_USER, "wrong_password")).andExpect(unauthenticated());
  }

  @Test
  void loginAsUnknownUserFails() throws Exception {
    mockMvc.perform(login("someone_else", "any_password")).andExpect(unauthenticated());
  }

  @Test
  void apiRequestWithValidTokenIsAuthenticatedAsTokenOwner() throws Exception {
    mockMvc
        .perform(get("/api/studies").header("Authorization", "Bearer " + VALID_TOKEN))
        .andExpect(
            authenticated()
                .withUsername(TOKEN_USER)
                .withAuthorities(AuthorityUtils.createAuthorityList("PRIVATE_GROUP")));
  }

  @Test
  void apiRequestWithUnknownTokenIsRejected() throws Exception {
    mockMvc
        .perform(get("/api/studies").header("Authorization", "Bearer " + UNKNOWN_TOKEN))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void apiRequestWithoutCredentialsIsRejected() throws Exception {
    mockMvc.perform(get("/api/studies")).andExpect(status().isUnauthorized());
  }

  private static RequestBuilder login(String username, String password) {
    return post("/j_spring_security_check")
        .param("j_username", username)
        .param("j_password", password);
  }
}
