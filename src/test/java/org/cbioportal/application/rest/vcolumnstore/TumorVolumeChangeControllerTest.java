package org.cbioportal.application.rest.vcolumnstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.cbioportal.domain.clinical_event.TumorVolumeChange;
import org.cbioportal.domain.clinical_event.usecase.GetTumorVolumeChange;
import org.cbioportal.legacy.web.config.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(
    classes = {
      TumorVolumeChangeController.class,
      TestConfig.class,
      TumorVolumeChangeControllerTest.SecurityConfig.class
    })
public class TumorVolumeChangeControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private GetTumorVolumeChange useCase;
  @MockitoBean private PermissionEvaluator permissionEvaluator;

  @Before
  public void allowStudyAccess() {
    when(permissionEvaluator.hasPermission(any(), eq("study"), eq("CancerStudyId"), any()))
        .thenReturn(true);
  }

  @TestConfiguration
  @EnableMethodSecurity
  static class SecurityConfig {
    @Bean
    static DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler(
        PermissionEvaluator permissionEvaluator) {
      var handler = new DefaultMethodSecurityExpressionHandler();
      handler.setPermissionEvaluator(permissionEvaluator);
      return handler;
    }
  }

  @Test
  @WithMockUser
  public void returnsResultsWithDefaultBaseline() throws Exception {
    when(useCase.calculateTumorVolumePercentageChange("study", 0, 30))
        .thenReturn(List.of(new TumorVolumeChange("study", "patient", -25)));

    mockMvc
        .perform(get("/api/studies/study/tumor-volume-changes").param("measurementTimepoint", "30"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].studyId").value("study"))
        .andExpect(jsonPath("$[0].patientId").value("patient"))
        .andExpect(jsonPath("$[0].percentageChange").value(-25));
  }

  @Test
  @WithMockUser
  public void acceptsExplicitBaseline() throws Exception {
    when(useCase.calculateTumorVolumePercentageChange("study", 10, 30)).thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/studies/study/tumor-volume-changes")
                .param("baselineTimepoint", "10")
                .param("measurementTimepoint", "30"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));

    verify(useCase).calculateTumorVolumePercentageChange("study", 10, 30);
  }

  @Test
  @WithMockUser
  public void requiresMeasurementTimepoint() throws Exception {
    mockMvc
        .perform(get("/api/studies/study/tumor-volume-changes"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void rejectsNonIntegerTimepoint() throws Exception {
    mockMvc
        .perform(
            get("/api/studies/study/tumor-volume-changes").param("measurementTimepoint", "abc"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void deniesUnauthorizedStudyBeforeCallingUseCase() throws Exception {
    when(permissionEvaluator.hasPermission(any(), eq("study"), eq("CancerStudyId"), any()))
        .thenReturn(false);

    mockMvc
        .perform(get("/api/studies/study/tumor-volume-changes").param("measurementTimepoint", "30"))
        .andExpect(status().isForbidden());

    verifyNoInteractions(useCase);
  }
}
