package org.cbioportal.application.rest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.cbioportal.domain.wsi.repository.WsiHierarchyRepository;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.web.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(
    classes = {
      WsiHierarchyController.class,
      TestConfig.class,
      WsiHierarchyControllerTest.MethodSecurityTestConfig.class
    })
public class WsiHierarchyControllerTest {

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

  @MockitoBean private WsiHierarchyRepository repository;

  @MockitoBean private CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator;

  @Test
  public void returnsUnauthorizedWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/api/wsi/hierarchy/study/patient")).andExpect(status().isUnauthorized());
    verifyNoInteractions(repository);
  }

  @Test
  @WithMockUser
  public void returnsForbiddenWhenStudyAccessIsDenied() throws Exception {
    when(cancerStudyPermissionEvaluator.hasPermission(
            org.mockito.ArgumentMatchers.any(Authentication.class),
            eq("study"),
            eq("CancerStudyId"),
            eq(AccessLevel.READ)))
        .thenReturn(false);

    mockMvc.perform(get("/api/wsi/hierarchy/study/patient")).andExpect(status().isForbidden());
    verifyNoInteractions(repository);
  }

  @Test
  @WithMockUser
  public void returnsMaterializedJsonForAuthorizedUsers() throws Exception {
    when(cancerStudyPermissionEvaluator.hasPermission(
            org.mockito.ArgumentMatchers.any(Authentication.class),
            eq("study"),
            eq("CancerStudyId"),
            eq(AccessLevel.READ)))
        .thenReturn(true);
    when(repository.getPatientHierarchy("study", "patient"))
        .thenReturn(
            "{\"patient_id\":\"patient\",\"samples\":[],\"slide_associations\":[{\"image_id\":\"slide-1\",\"sample_id\":\"sample-1\",\"match_level\":\"PART\",\"specimen_key\":\"part::1\",\"slide_type\":\"H&E\",\"can_serve_tiles\":true}]}");

    mockMvc
        .perform(get("/api/wsi/hierarchy/study/patient"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(content().json(
            "{\"patient_id\":\"patient\",\"samples\":[],\"slide_associations\":[{\"image_id\":\"slide-1\",\"sample_id\":\"sample-1\",\"match_level\":\"PART\",\"specimen_key\":\"part::1\",\"slide_type\":\"H&E\",\"can_serve_tiles\":true}]}"));
  }

  @Test
  @WithMockUser
  public void returnsBootstrapEnvelopeForAuthorizedUsers() throws Exception {
    when(cancerStudyPermissionEvaluator.hasPermission(
            org.mockito.ArgumentMatchers.any(Authentication.class),
            eq("study"),
            eq("CancerStudyId"),
            eq(AccessLevel.READ)))
        .thenReturn(true);
    when(repository.getPatientHierarchy("study", "patient"))
        .thenReturn("{\"patient_id\":\"patient\",\"samples\":[]}");

    mockMvc
        .perform(get("/api/wsi/hierarchy/study/patient/bootstrap"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(
            content()
                .json("{\"hierarchy\":{\"patient_id\":\"patient\",\"samples\":[]},\"initial\":null}"));
  }

  @Test
  @WithMockUser
  public void returnsNotFoundWhenPatientIsMissing() throws Exception {
    when(cancerStudyPermissionEvaluator.hasPermission(
            org.mockito.ArgumentMatchers.any(Authentication.class),
            eq("study"),
            eq("CancerStudyId"),
            eq(AccessLevel.READ)))
        .thenReturn(true);
    when(repository.getPatientHierarchy("study", "missing")).thenReturn(null);

    mockMvc
        .perform(get("/api/wsi/hierarchy/study/missing"))
        .andExpect(status().isNotFound());
  }
}
