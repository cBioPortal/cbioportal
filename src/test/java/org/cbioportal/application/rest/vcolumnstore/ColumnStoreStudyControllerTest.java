package org.cbioportal.application.rest.vcolumnstore;

import java.util.List;
import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.cbioportal.domain.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.cbioportal.shared.enums.ProjectionType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ColumnStoreStudyControllerTest {

  private GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;
  private PermissionEvaluator permissionEvaluator;
  private MockMvc mockMvc;

  @Before
  public void setUp() {
    getCancerStudyMetadataUseCase = Mockito.mock(GetCancerStudyMetadataUseCase.class);
    permissionEvaluator = Mockito.mock(PermissionEvaluator.class);

    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new ColumnStoreStudyController(getCancerStudyMetadataUseCase, permissionEvaluator))
            .build();
  }

  @Test
  public void getAllStudies_evaluatesAndMapsReadPermissionsCorrectly() throws Exception {
    CancerStudyMetadata authorizedStudy = Mockito.mock(CancerStudyMetadata.class);
    Mockito.when(authorizedStudy.cancerStudyIdentifier()).thenReturn("auth_study_id");

    CancerStudyMetadata unauthorizedStudy = Mockito.mock(CancerStudyMetadata.class);
    Mockito.when(unauthorizedStudy.cancerStudyIdentifier()).thenReturn("unauth_study_id");

    Mockito.when(
            getCancerStudyMetadataUseCase.execute(
                Mockito.any(ProjectionType.class), Mockito.any(SortAndSearchCriteria.class)))
        .thenReturn(List.of(authorizedStudy, unauthorizedStudy));

    Mockito.when(
            permissionEvaluator.hasPermission(
                Mockito.any(Authentication.class),
                Mockito.eq("auth_study_id"),
                Mockito.eq("CancerStudyId"),
                Mockito.eq(AccessLevel.READ)))
        .thenReturn(true);

    Mockito.when(
            permissionEvaluator.hasPermission(
                Mockito.any(Authentication.class),
                Mockito.eq("unauth_study_id"),
                Mockito.eq("CancerStudyId"),
                Mockito.eq(AccessLevel.READ)))
        .thenReturn(false);

    // Create a mock Authentication token and inject it into the test Security Context
    org.springframework.security.core.Authentication mockAuth =
        Mockito.mock(org.springframework.security.core.Authentication.class);
    org.springframework.security.core.context.SecurityContextHolder.getContext()
        .setAuthentication(mockAuth);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/studies")
                .param("projection", "SUMMARY")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("auth_study_id"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].readPermission").value(true))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value("unauth_study_id"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].readPermission").value(false));
  }
}
