package org.cbioportal.application.rest.vcolumnstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.List;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTabsRequest;
import org.cbioportal.domain.resource.usecase.GetResourceTableDataUseCase;
import org.cbioportal.domain.resource.usecase.GetResourceTableMetadataUseCase;
import org.cbioportal.domain.resource.usecase.GetResourceTableTabsUseCase;
import org.cbioportal.legacy.web.config.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Authorization tests for {@link ResourceTableController}.
 *
 * <p>These exist because the endpoints once denied <em>every</em> request when authentication was
 * on. They authorized against the {@code involvedCancerStudies} request attribute, which {@code
 * InvolvedCancerStudyExtractorInterceptor} never populates for this controller: it returns early
 * for the whole {@code vcolumnstore} package, since column-store endpoints authorize themselves.
 * The attribute stayed null and {@code hasPermission()} denies a null target.
 *
 * <p>{@link ResourceTableControllerTest} could not catch it — it does not enable method security,
 * so {@code @PreAuthorize} never runs there. These tests turn it on and assert on what the {@link
 * PermissionEvaluator} actually receives, which is the part that was wrong.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(
    classes = {
      ResourceTableController.class,
      TestConfig.class,
      ResourceTableControllerSecurityTest.MethodSecurityTestConfig.class
    })
public class ResourceTableControllerSecurityTest {

  private static final String STUDY_ID = "study_tcga_pub";

  @TestConfiguration
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

  @MockitoBean private GetResourceTableTabsUseCase getResourceTableTabsUseCase;
  @MockitoBean private GetResourceTableDataUseCase getResourceTableDataUseCase;
  @MockitoBean private GetResourceTableMetadataUseCase getResourceTableMetadataUseCase;

  @Autowired private MockMvc mockMvc;
  @Autowired private PermissionEvaluator permissionEvaluator;

  private final ObjectMapper objectMapper = new ObjectMapper();

  // permissionEvaluator is a plain @Bean mock, not @MockitoBean, so Spring does not reset it
  // between tests and invocation counts would accumulate across the class.
  @Before
  public void resetPermissionEvaluator() {
    Mockito.reset(permissionEvaluator);
  }

  private void grant(boolean allowed) {
    Mockito.when(
            permissionEvaluator.hasPermission(
                Mockito.any(Authentication.class),
                Mockito.<Serializable>any(),
                Mockito.anyString(),
                Mockito.any()))
        .thenReturn(allowed);
  }

  /** The target handed to the evaluator, which was null before the endpoints authorized on body. */
  private Serializable capturedPermissionTarget() {
    ArgumentCaptor<Serializable> target = ArgumentCaptor.forClass(Serializable.class);
    Mockito.verify(permissionEvaluator)
        .hasPermission(
            Mockito.any(Authentication.class),
            target.capture(),
            Mockito.anyString(),
            Mockito.any());
    return target.getValue();
  }

  @Test
  @WithMockUser
  public void tabsFetch_permittedUser_isAllowedAndCheckedAgainstTheRequestedStudies()
      throws Exception {
    grant(true);
    Mockito.when(getResourceTableTabsUseCase.execute(Mockito.any())).thenReturn(List.of());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/resource-table/tabs/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new ResourceTabsRequest(List.of(STUDY_ID), null, null))))
        .andExpect(MockMvcResultMatchers.status().isOk());

    assertThat(capturedPermissionTarget())
        .as("study ids the permission check was given")
        .isEqualTo(List.of(STUDY_ID));
  }

  @Test
  @WithMockUser
  public void tabsFetch_userWithoutAccess_isForbidden() throws Exception {
    grant(false);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/resource-table/tabs/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new ResourceTabsRequest(List.of(STUDY_ID), null, null))))
        .andExpect(MockMvcResultMatchers.status().isForbidden());
  }

  @Test
  @WithMockUser
  public void queryFetch_permittedUser_isAllowedAndCheckedAgainstTheRequestedStudies()
      throws Exception {
    grant(true);
    Mockito.when(getResourceTableDataUseCase.execute(Mockito.any())).thenReturn(null);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/resource-table/query/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new ResourceTableQuery(
                            List.of(STUDY_ID),
                            "HE_SLIDE",
                            null,
                            null,
                            null,
                            0,
                            25,
                            null,
                            null,
                            null))))
        .andExpect(MockMvcResultMatchers.status().isOk());

    assertThat(capturedPermissionTarget()).isEqualTo(List.of(STUDY_ID));
  }

  @Test
  @WithMockUser
  public void queryFetch_userWithoutAccess_isForbidden() throws Exception {
    grant(false);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/resource-table/query/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new ResourceTableQuery(
                            List.of(STUDY_ID),
                            "HE_SLIDE",
                            null,
                            null,
                            null,
                            0,
                            25,
                            null,
                            null,
                            null))))
        .andExpect(MockMvcResultMatchers.status().isForbidden());
  }
}
