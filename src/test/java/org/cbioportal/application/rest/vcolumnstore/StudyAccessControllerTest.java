package org.cbioportal.application.rest.vcolumnstore;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.Serializable;
import java.util.Optional;
import org.cbioportal.application.security.config.ApiSecurityConfig;
import org.cbioportal.domain.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.web.config.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Tests {@link StudyAccessController} on a portal that requires authentication, with the real API
 * filter chain ({@link ApiSecurityConfig}) and method security backed by a mocked {@link
 * PermissionEvaluator}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@TestPropertySource(properties = {"authenticate=oauth2", "dat.method=none"})
@ContextConfiguration(
    classes = {
      StudyAccessController.class,
      ApiSecurityConfig.class,
      TestConfig.class,
      StudyAccessControllerTest.MethodSecurityTestConfig.class
    })
public class StudyAccessControllerTest {

  private static final String STUDY_ID = "study_tcga_pub";
  private static final String UNKNOWN_STUDY_ID = "no_such_study";
  private static final String URL = "/api/studies/{studyId}/access";

  @TestConfiguration
  @EnableWebSecurity
  @EnableMethodSecurity
  static class MethodSecurityTestConfig {

    @Bean
    PermissionEvaluator permissionEvaluator() {
      return Mockito.mock(PermissionEvaluator.class);
    }

    @Bean
    MethodSecurityExpressionHandler methodSecurityExpressionHandler(
        PermissionEvaluator permissionEvaluator) {
      DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
      handler.setPermissionEvaluator(permissionEvaluator);
      return handler;
    }
  }

  @MockitoBean private GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;

  @Autowired private WebApplicationContext context;
  @Autowired private PermissionEvaluator permissionEvaluator;

  private MockMvc mockMvc;

  // permissionEvaluator is a plain @Bean mock, not @MockitoBean, so Spring does not reset it
  // between tests.
  @Before
  public void setUp() {
    // Built here rather than autowired so the filter chain sees @WithMockUser users.
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    Mockito.reset(permissionEvaluator);
    Mockito.when(getCancerStudyMetadataUseCase.findStudyIdentifier(STUDY_ID))
        .thenReturn(Optional.of(STUDY_ID));
    Mockito.when(getCancerStudyMetadataUseCase.findStudyIdentifier(UNKNOWN_STUDY_ID))
        .thenReturn(Optional.empty());
  }

  /** Grants or denies READ on {@code studyId}, as the real evaluator would for that user. */
  private void grantRead(String studyId, boolean allowed) {
    Mockito.when(
            permissionEvaluator.hasPermission(
                Mockito.any(Authentication.class),
                Mockito.eq((Serializable) studyId),
                Mockito.eq("CancerStudyId"),
                Mockito.eq(AccessLevel.READ)))
        .thenReturn(allowed);
  }

  @Test
  @WithMockUser(username = "reader")
  public void authorizedUser_getsNoContentWithUserAndStudyHeaders() throws Exception {
    grantRead(STUDY_ID, true);

    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, STUDY_ID))
        .andExpect(status().isNoContent())
        .andExpect(header().string(StudyAccessController.USER_HEADER, "reader"))
        .andExpect(header().string(StudyAccessController.STUDY_HEADER, STUDY_ID))
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(content().string(""));
  }

  @Test
  public void unauthenticatedRequest_isUnauthorized() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, STUDY_ID))
        .andExpect(status().isUnauthorized())
        .andExpect(header().doesNotExist(StudyAccessController.USER_HEADER))
        .andExpect(header().doesNotExist(StudyAccessController.STUDY_HEADER));

    Mockito.verifyNoInteractions(permissionEvaluator, getCancerStudyMetadataUseCase);
  }

  @Test
  @WithMockUser(username = "reader")
  public void userWithoutAccess_isForbiddenWithoutBody() throws Exception {
    grantRead(STUDY_ID, false);

    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, STUDY_ID))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(StudyAccessController.USER_HEADER))
        .andExpect(header().doesNotExist(StudyAccessController.STUDY_HEADER))
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(content().string(""));

    Mockito.verifyNoInteractions(getCancerStudyMetadataUseCase);
  }

  @Test
  @WithMockUser(username = "reader")
  public void unknownStudy_isForbiddenNotNotFound() throws Exception {
    // CancerStudyPermissionEvaluator denies ids missing from its study cache.
    grantRead(UNKNOWN_STUDY_ID, false);

    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, UNKNOWN_STUDY_ID))
        .andExpect(status().isForbidden())
        .andExpect(content().string(""));
  }

  @Test
  @WithMockUser(username = "reader")
  public void studyMissingFromDatabase_isForbiddenEvenIfPermitted() throws Exception {
    // The evaluator's study cache can be stale after a study is deleted.
    grantRead(UNKNOWN_STUDY_ID, true);

    mockMvc
        .perform(MockMvcRequestBuilders.get(URL, UNKNOWN_STUDY_ID))
        .andExpect(status().isForbidden())
        .andExpect(header().doesNotExist(StudyAccessController.USER_HEADER))
        .andExpect(content().string(""));
  }
}
