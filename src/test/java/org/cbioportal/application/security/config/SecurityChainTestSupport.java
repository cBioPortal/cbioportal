package org.cbioportal.application.security.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.cbioportal.application.rest.availability.UnavailableStudyIdentifiers;
import org.cbioportal.application.rest.vcolumnstore.ColumnStoreStudyController;
import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.cbioportal.domain.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.persistence.SecurityRepository;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.service.ServerStatusService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.web.ServerStatusController;
import org.cbioportal.legacy.web.StudyController;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Shared setup for tests that run the production security filter chains and method security against
 * real controllers, with the database replaced by mocks.
 *
 * <p>Subclasses import the security configuration of one property combination and set its
 * properties. Studies: {@link #PUBLIC_STUDY} (group PUBLIC), {@link #TEAM_STUDY} (group TEAM_A) and
 * {@link #PRIVATE_STUDY} (no group).
 */
abstract class SecurityChainTestSupport {

  static final String PUBLIC_STUDY = "public_study";
  static final String TEAM_STUDY = "team_study";
  static final String PRIVATE_STUDY = "private_study";

  static final String SAML_REGISTRATION_ID = "keycloak";
  static final String SAML_IDP_SSO_URL = "https://idp.example.org/saml/sso";
  static final String SAML_IDP_SLO_URL = "https://idp.example.org/saml/slo";

  static final String GOOGLE_REGISTRATION_ID = "google";
  static final String KEYCLOAK_REGISTRATION_ID = "keycloak";
  static final String OIDC_END_SESSION_URL = "https://keycloak.example.org/logout";

  /** Real API controllers plus mocks for the services and caches they and security read. */
  @Configuration
  @EnableWebMvc
  @Import({
    StudyController.class,
    ColumnStoreStudyController.class,
    ServerStatusController.class,
    CorsConfig.class
  })
  static class ApiBeans {
    @Bean
    StudyService studyService() {
      StudyService studyService = Mockito.mock(StudyService.class);
      try {
        Mockito.when(studyService.getStudy(Mockito.anyString()))
            .thenAnswer(invocation -> study(invocation.getArgument(0), ""));
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
      return studyService;
    }

    @Bean
    GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase() {
      GetCancerStudyMetadataUseCase useCase = Mockito.mock(GetCancerStudyMetadataUseCase.class);
      try {
        Mockito.when(useCase.getStudy(Mockito.anyString()))
            .thenAnswer(invocation -> Mockito.mock(CancerStudyMetadata.class));
      } catch (Exception e) {
        throw new IllegalStateException(e);
      }
      return useCase;
    }

    @Bean
    UnavailableStudyIdentifiers unavailableStudyIdentifiers() {
      return Mockito.mock(UnavailableStudyIdentifiers.class);
    }

    @Bean
    ServerStatusService serverStatusService() {
      return Mockito.mock(ServerStatusService.class);
    }

    @Bean
    CacheMapUtil cacheMapUtil() {
      CacheMapUtil cacheMapUtil = Mockito.mock(CacheMapUtil.class);
      Mockito.when(cacheMapUtil.getCancerStudyPermissionMap())
          .thenReturn(
              Map.of(
                  PUBLIC_STUDY, study(PUBLIC_STUDY, "PUBLIC"),
                  TEAM_STUDY, study(TEAM_STUDY, "TEAM_A"),
                  PRIVATE_STUDY, study(PRIVATE_STUDY, "")));
      return cacheMapUtil;
    }

    @Bean
    SecurityRepository securityRepository() {
      return Mockito.mock(SecurityRepository.class);
    }
  }

  /** A SAML identity provider that supports single logout, as the Keycloak realms do. */
  @Configuration
  static class SamlIdpBeans {
    @Bean
    RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() throws Exception {
      Saml2X509Credential signing = signingCredential();
      return new InMemoryRelyingPartyRegistrationRepository(
          RelyingPartyRegistration.withRegistrationId(SAML_REGISTRATION_ID)
              .entityId("cbioportal")
              .signingX509Credentials(credentials -> credentials.add(signing))
              .singleLogoutServiceLocation("{baseUrl}/logout/saml2/slo")
              .assertingPartyMetadata(
                  idp ->
                      idp.entityId("https://idp.example.org")
                          .singleSignOnServiceLocation(SAML_IDP_SSO_URL)
                          .singleLogoutServiceLocation(SAML_IDP_SLO_URL)
                          .verificationX509Credentials(
                              credentials ->
                                  credentials.add(
                                      Saml2X509Credential.verification(signing.getCertificate()))))
              .build());
    }

    private static Saml2X509Credential signingCredential() throws Exception {
      RSAPrivateKey key;
      try (InputStream in = new ClassPathResource("security/signing-key.pem").getInputStream()) {
        key = RsaKeyConverters.pkcs8().convert(in);
      }
      X509Certificate certificate;
      try (InputStream in = new ClassPathResource("security/signing-cert.pem").getInputStream()) {
        certificate =
            (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
      }
      return Saml2X509Credential.signing(key, certificate);
    }
  }

  /**
   * OIDC providers: one without an end_session_endpoint, as Google, and one with it, as Keycloak
   * and Microsoft Entra.
   */
  @Configuration
  static class OidcIdpBeans {
    @Bean
    ClientRegistrationRepository clientRegistrationRepository() {
      return new InMemoryClientRegistrationRepository(
          oidcRegistration(GOOGLE_REGISTRATION_ID).build(),
          oidcRegistration(KEYCLOAK_REGISTRATION_ID)
              .providerConfigurationMetadata(Map.of("end_session_endpoint", OIDC_END_SESSION_URL))
              .build());
    }

    private static ClientRegistration.Builder oidcRegistration(String registrationId) {
      return ClientRegistration.withRegistrationId(registrationId)
          .clientId("cbioportal")
          .clientSecret("secret")
          .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
          .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
          .scope("openid", "email")
          .authorizationUri("https://" + registrationId + ".example.org/auth")
          .tokenUri("https://" + registrationId + ".example.org/token")
          .jwkSetUri("https://" + registrationId + ".example.org/certs")
          .userNameAttributeName("email");
    }
  }

  @Autowired private WebApplicationContext context;

  MockMvc mockMvc;

  @BeforeEach
  void setUpMockMvc() {
    // springSecurity() copies this context into each request; other tests leave mock users in it
    TestSecurityContextHolder.clearContext();
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  /** A user signed in through the SAML identity provider, holding the given roles. */
  static RequestPostProcessor samlUser(String... roles) {
    DefaultSaml2AuthenticatedPrincipal principal =
        new DefaultSaml2AuthenticatedPrincipal(
            "saml-user@example.org", Map.of("Role", List.of((Object[]) roles)));
    principal.setRelyingPartyRegistrationId(SAML_REGISTRATION_ID);
    return authentication(
        new Saml2Authentication(
            principal,
            "<saml2p:Response/>",
            Arrays.stream(roles).map(SimpleGrantedAuthority::new).collect(Collectors.toSet())));
  }

  /** A user signed in through the given OIDC provider, holding the given authorities. */
  static RequestPostProcessor oidcUser(
      ClientRegistrationRepository registrations, String registrationId, String... authorities) {
    return oidcLogin()
        .clientRegistration(registrations.findByRegistrationId(registrationId))
        .idToken(token -> token.claim("email", "oidc-user@example.org"))
        .authorities(
            Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));
  }

  static RequestPostProcessor bearer(String token) {
    return request -> {
      request.addHeader("Authorization", "Bearer " + token);
      return request;
    };
  }

  static CancerStudy study(String id, String groups) {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier(id);
    study.setGroups(groups);
    return study;
  }
}
