
package org.cbioportal.application.rest.vcolumnstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.cbioportal.application.rest.availability.UnavailableStudyIdentifiers;
import org.cbioportal.application.rest.response.CancerStudyMetadataDTO;
import org.cbioportal.domain.cancerstudy.CancerStudyMetadata;
import org.cbioportal.domain.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.cbioportal.shared.enums.ProjectionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Unit tests for {@link ColumnStoreStudyController}.
 *
 * <p>Covers pagination and read-permission mapping, including the rule that locked studies are
 * always reported as unreadable, regardless of the study availability configuration.
 */
class ColumnStoreStudyControllerTest {

  private static final int TOTAL_STUDIES = 25;

  private GetCancerStudyMetadataUseCase useCase;
  private PermissionEvaluator permissionEvaluator;
  private UnavailableStudyIdentifiers unavailableStudyIdentifiers;
  private ColumnStoreStudyController controller;
  private MockMvc mockMvc;

  private static CancerStudyMetadata stubStudy(String identifier) {
    return new CancerStudyMetadata(
        null, identifier, null, identifier, null, true, null, null, null, null, null, null);
  }

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();

    useCase = mock(GetCancerStudyMetadataUseCase.class);
    permissionEvaluator = mock(PermissionEvaluator.class);
    unavailableStudyIdentifiers = mock(UnavailableStudyIdentifiers.class);

    when(unavailableStudyIdentifiers.get()).thenReturn(Map.of());

    controller = new ColumnStoreStudyController(useCase, null, unavailableStudyIdentifiers, true);

    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new ColumnStoreStudyController(
                    useCase, permissionEvaluator, unavailableStudyIdentifiers, true))
            .build();

    List<CancerStudyMetadata> allStudies =
        IntStream.range(0, TOTAL_STUDIES).mapToObj(i -> stubStudy("study-" + i)).toList();

    when(useCase.execute(any(ProjectionType.class), any())).thenReturn(allStudies);
  }

  // --------------------------------------------------------------------------
  // Read-permission mapping
  // --------------------------------------------------------------------------

  @Test
  void getAllStudies_evaluatesAndMapsReadPermissionsCorrectly() throws Exception {
    CancerStudyMetadata authorizedStudy = Mockito.mock(CancerStudyMetadata.class);
    Mockito.when(authorizedStudy.cancerStudyIdentifier()).thenReturn("auth_study_id");
    Mockito.when(authorizedStudy.status()).thenReturn(1);

    CancerStudyMetadata unauthorizedStudy = Mockito.mock(CancerStudyMetadata.class);
    Mockito.when(unauthorizedStudy.cancerStudyIdentifier()).thenReturn("unauth_study_id");
    Mockito.when(unauthorizedStudy.status()).thenReturn(1);

    Mockito.when(
            useCase.execute(
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

    SecurityContextHolder.getContext().setAuthentication(Mockito.mock(Authentication.class));

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

  @Test
  void getAllStudies_whenSecurityIsDisabled_defaultsReadPermissionToTrue() throws Exception {
    CancerStudyMetadata testStudy = Mockito.mock(CancerStudyMetadata.class);
    Mockito.when(testStudy.cancerStudyIdentifier()).thenReturn("public_study");
    Mockito.when(testStudy.status()).thenReturn(1);

    Mockito.when(
            useCase.execute(
                Mockito.any(ProjectionType.class), Mockito.any(SortAndSearchCriteria.class)))
        .thenReturn(List.of(testStudy));

    MockMvc mockMvcNoSecurity =
        MockMvcBuilders.standaloneSetup(
                new ColumnStoreStudyController(useCase, null, unavailableStudyIdentifiers, true))
            .build();

    mockMvcNoSecurity
        .perform(
            MockMvcRequestBuilders.get("/api/studies")
                .param("projection", "SUMMARY")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("public_study"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].readPermission").value(true));
  }

  @Test
  void getAllStudies_whenStudyIsLocked_readPermissionIsFalse() throws Exception {
    CancerStudyMetadata lockedStudy = Mockito.mock(CancerStudyMetadata.class);
    Mockito.when(lockedStudy.cancerStudyIdentifier()).thenReturn("locked_study");
    Mockito.when(lockedStudy.status()).thenReturn(0);

    Mockito.when(
            useCase.execute(
                Mockito.any(ProjectionType.class), Mockito.any(SortAndSearchCriteria.class)))
        .thenReturn(List.of(lockedStudy));

    Mockito.when(
            permissionEvaluator.hasPermission(
                Mockito.any(Authentication.class),
                Mockito.eq("locked_study"),
                Mockito.eq("CancerStudyId"),
                Mockito.eq(AccessLevel.READ)))
        .thenReturn(true);

    SecurityContextHolder.getContext().setAuthentication(Mockito.mock(Authentication.class));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/studies")
                .param("projection", "SUMMARY")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("locked_study"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].readPermission").value(false));
  }

  @Test
  void getAllStudies_whenAvailabilityEnabled_unavailableStudyIsUnreadableInEveryAuthMode()
      throws Exception {
    CancerStudyMetadata available = Mockito.mock(CancerStudyMetadata.class);
    Mockito.when(available.cancerStudyIdentifier()).thenReturn("available_study");
    Mockito.when(available.status()).thenReturn(1);

    CancerStudyMetadata unavailable = Mockito.mock(CancerStudyMetadata.class);
    Mockito.when(unavailable.cancerStudyIdentifier()).thenReturn("unavailable_study");
    Mockito.when(unavailable.status()).thenReturn(1);

    Mockito.when(
            useCase.execute(
                Mockito.any(ProjectionType.class), Mockito.any(SortAndSearchCriteria.class)))
        .thenReturn(List.of(available, unavailable));

    when(unavailableStudyIdentifiers.get())
        .thenReturn(Map.of("unavailable_study", "unavailable_study"));

    Mockito.when(
            permissionEvaluator.hasPermission(
                Mockito.any(Authentication.class),
                Mockito.any(),
                Mockito.eq("CancerStudyId"),
                Mockito.eq(AccessLevel.READ)))
        .thenReturn(true);

    SecurityContextHolder.getContext().setAuthentication(Mockito.mock(Authentication.class));

    MockMvc noSecurity =
        MockMvcBuilders.standaloneSetup(
                new ColumnStoreStudyController(useCase, null, unavailableStudyIdentifiers, true))
            .build();

    for (MockMvc mvc : List.of(mockMvc, noSecurity)) {
      mvc.perform(
              MockMvcRequestBuilders.get("/api/studies")
                  .param("projection", "SUMMARY")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].readPermission").value(true))
          .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value("unavailable_study"))
          .andExpect(MockMvcResultMatchers.jsonPath("$[1].readPermission").value(false));
    }
  }

  @Test
  void getAllStudies_whenAvailabilityDisabled_lockedStudyIsUnreadable() throws Exception {
    CancerStudyMetadata lockedStudy = Mockito.mock(CancerStudyMetadata.class);
    Mockito.when(lockedStudy.cancerStudyIdentifier()).thenReturn("locked_study");
    Mockito.when(lockedStudy.status()).thenReturn(0);

    Mockito.when(
            useCase.execute(
                Mockito.any(ProjectionType.class), Mockito.any(SortAndSearchCriteria.class)))
        .thenReturn(List.of(lockedStudy));

    when(unavailableStudyIdentifiers.get()).thenReturn(Map.of("locked_study", "locked_study"));

    Mockito.when(
            permissionEvaluator.hasPermission(
                Mockito.any(Authentication.class),
                Mockito.any(),
                Mockito.eq("CancerStudyId"),
                Mockito.eq(AccessLevel.READ)))
        .thenReturn(true);

    SecurityContextHolder.getContext().setAuthentication(Mockito.mock(Authentication.class));

    MockMvc secured =
        MockMvcBuilders.standaloneSetup(
                new ColumnStoreStudyController(
                    useCase, permissionEvaluator, unavailableStudyIdentifiers, false))
            .build();

    MockMvc noSecurity =
        MockMvcBuilders.standaloneSetup(
                new ColumnStoreStudyController(useCase, null, unavailableStudyIdentifiers, false))
            .build();

    for (MockMvc mvc : List.of(secured, noSecurity)) {
      mvc.perform(
              MockMvcRequestBuilders.get("/api/studies")
                  .param("projection", "SUMMARY")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(MockMvcResultMatchers.status().isOk())
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("locked_study"))
          .andExpect(MockMvcResultMatchers.jsonPath("$[0].readPermission").value(false));
    }

    Mockito.verify(unavailableStudyIdentifiers, Mockito.never()).get();
  }

  // --------------------------------------------------------------------------
  // No pagination
  // --------------------------------------------------------------------------

  @Test
  void getAllStudies_noPagination_returnsAllStudies() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, null, null, Direction.ASC);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(TOTAL_STUDIES);
  }

  // --------------------------------------------------------------------------
  // Pagination
  // --------------------------------------------------------------------------

  @Test
  void getAllStudies_pageNumberZero_treatedAsFirstPage() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, 10, 0, Direction.ASC);

    List<CancerStudyMetadataDTO> body = response.getBody();
    assertThat(body).hasSize(10);
    assertThat(body.get(0).studyId()).isEqualTo("study-0");
    assertThat(body.get(9).studyId()).isEqualTo("study-9");
  }

  @Test
  void getAllStudies_pageSizeOnly_defaultsToFirstPage() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, 10, null, Direction.ASC);

    List<CancerStudyMetadataDTO> body = response.getBody();
    assertThat(body).hasSize(10);
    assertThat(body.get(0).studyId()).isEqualTo("study-0");
    assertThat(body.get(9).studyId()).isEqualTo("study-9");
  }

  @Test
  void getAllStudies_pageNumber1_returnsFirstPage() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, 10, 1, Direction.ASC);

    List<CancerStudyMetadataDTO> body = response.getBody();
    assertThat(body).hasSize(10);
    assertThat(body.get(0).studyId()).isEqualTo("study-0");
    assertThat(body.get(9).studyId()).isEqualTo("study-9");
  }

  @Test
  void getAllStudies_pageNumber2_returnsSecondPage() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, 10, 2, Direction.ASC);

    List<CancerStudyMetadataDTO> body = response.getBody();
    assertThat(body).hasSize(10);
    assertThat(body.get(0).studyId()).isEqualTo("study-10");
    assertThat(body.get(9).studyId()).isEqualTo("study-19");
  }

  @Test
  void getAllStudies_lastPartialPage_returnsRemainingStudies() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, 10, 3, Direction.ASC);

    List<CancerStudyMetadataDTO> body = response.getBody();
    assertThat(body).hasSize(5);
    assertThat(body.get(0).studyId()).isEqualTo("study-20");
    assertThat(body.get(4).studyId()).isEqualTo("study-24");
  }

  @Test
  void getAllStudies_pageNumberBeyondEnd_returnsEmptyList() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, 10, 4, Direction.ASC);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  void getAllStudies_metaProjectionWithPagination_totalCountHeaderReportsFullCount() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.META, null, 10, 2, Direction.ASC);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
    assertThat(response.getHeaders().getFirst("X-Total-Count"))
        .isEqualTo(String.valueOf(TOTAL_STUDIES));
    assertThat(response.getHeaders().getFirst("total-count"))
        .isEqualTo(String.valueOf(TOTAL_STUDIES));
  }
}
