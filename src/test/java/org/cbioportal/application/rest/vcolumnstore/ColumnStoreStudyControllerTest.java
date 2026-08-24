package org.cbioportal.application.rest.vcolumnstore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.IntStream;
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
 * <p>Covers two areas:
 *
 * <ul>
 *   <li><b>Pagination</b> — verifies that {@code pageNumber} is correctly honoured and results are
 *       sliced with the right offset (prior to the fix, {@code stream().limit(pageSize)} always
 *       returned the first page regardless of which page was requested).
 *   <li><b>Read-permission mapping</b> — verifies that the {@code readPermission} field in the
 *       response DTO reflects the result of the {@link PermissionEvaluator} check, not the raw
 *       {@code publicStudy} flag.
 * </ul>
 */
class ColumnStoreStudyControllerTest {

  private static final int TOTAL_STUDIES = 25;

  private GetCancerStudyMetadataUseCase useCase;
  private PermissionEvaluator permissionEvaluator;

  /** Controller instance used by the direct-call pagination tests (no security evaluation). */
  private ColumnStoreStudyController controller;

  /** MockMvc instance used by the read-permission tests (with security evaluation). */
  private MockMvc mockMvc;

  /**
   * Creates a minimal {@link CancerStudyMetadata} using the compact constructor that takes only the
   * fields we care about for pagination tests.
   */
  private static CancerStudyMetadata stubStudy(String identifier) {
    return new CancerStudyMetadata(
        /* cancerStudyId           */ null,
        /* cancerStudyIdentifier   */ identifier,
        /* typeOfCancerId          */ null,
        /* name                    */ identifier,
        /* description             */ null,
        /* publicStudy             */ true,
        /* pmid                    */ null,
        /* citation                */ null,
        /* groups                  */ null,
        /* status                  */ null,
        /* importDate              */ null,
        /* referenceGenome         */ null);
  }

  @BeforeEach
  void setUp() {
    useCase = mock(GetCancerStudyMetadataUseCase.class);
    permissionEvaluator = mock(PermissionEvaluator.class);

    // Direct-call controller (null permissionEvaluator → security disabled, readPermission=true)
    controller = new ColumnStoreStudyController(useCase, null);

    // MockMvc controller (with permissionEvaluator for read-permission tests)
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new ColumnStoreStudyController(useCase, permissionEvaluator))
            .build();

    // Default stub: 25 studies for pagination tests (individual tests may override this)
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

    CancerStudyMetadata unauthorizedStudy = Mockito.mock(CancerStudyMetadata.class);
    Mockito.when(unauthorizedStudy.cancerStudyIdentifier()).thenReturn("unauth_study_id");

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

    Authentication mockAuth = Mockito.mock(Authentication.class);
    SecurityContextHolder.getContext().setAuthentication(mockAuth);

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

    Mockito.when(
            useCase.execute(
                Mockito.any(ProjectionType.class), Mockito.any(SortAndSearchCriteria.class)))
        .thenReturn(List.of(testStudy));

    MockMvc mockMvcNoSecurity =
        MockMvcBuilders.standaloneSetup(new ColumnStoreStudyController(useCase, null)).build();

    mockMvcNoSecurity
        .perform(
            MockMvcRequestBuilders.get("/api/studies")
                .param("projection", "SUMMARY")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("public_study"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].readPermission").value(true));
  }

  // --------------------------------------------------------------------------
  // No pagination
  // --------------------------------------------------------------------------

  /** When {@code pageSize} is {@code null} the full list must be returned untouched. */
  @Test
  void getAllStudies_noPagination_returnsAllStudies() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, null, null, Direction.ASC);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(TOTAL_STUDIES);
  }

  // --------------------------------------------------------------------------
  // First page
  // --------------------------------------------------------------------------

  /**
   * {@code pageNumber=0} must be treated as page 1 (offset 0) for backward compatibility and must
   * NOT throw an {@link IllegalArgumentException}.
   */
  @Test
  void getAllStudies_pageNumberZero_treatedAsFirstPage() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, 10, 0, Direction.ASC);

    List<CancerStudyMetadataDTO> body = response.getBody();
    assertThat(body).hasSize(10);
    assertThat(body.get(0).studyId()).isEqualTo("study-0");
    assertThat(body.get(9).studyId()).isEqualTo("study-9");
  }

  /** {@code pageSize=10, pageNumber=null} → offset defaults to 0, returns items 0–9. */
  @Test
  void getAllStudies_pageSizeOnly_defaultsToFirstPage() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, 10, null, Direction.ASC);

    List<CancerStudyMetadataDTO> body = response.getBody();
    assertThat(body).hasSize(10);
    assertThat(body.get(0).studyId()).isEqualTo("study-0");
    assertThat(body.get(9).studyId()).isEqualTo("study-9");
  }

  /** Explicit {@code pageNumber=1} must produce the same first-page slice. */
  @Test
  void getAllStudies_pageNumber1_returnsFirstPage() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, 10, 1, Direction.ASC);

    List<CancerStudyMetadataDTO> body = response.getBody();
    assertThat(body).hasSize(10);
    assertThat(body.get(0).studyId()).isEqualTo("study-0");
    assertThat(body.get(9).studyId()).isEqualTo("study-9");
  }

  // --------------------------------------------------------------------------
  // Second page — the primary regression test
  // --------------------------------------------------------------------------

  /**
   * {@code pageNumber=2, pageSize=10} must skip the first 10 items and return items 10–19.
   *
   * <p>This is the key regression test: the old {@code stream().limit(pageSize)} ignored {@code
   * pageNumber} entirely and always returned "study-0" … "study-9".
   */
  @Test
  void getAllStudies_pageNumber2_returnsSecondPage() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, 10, 2, Direction.ASC);

    List<CancerStudyMetadataDTO> body = response.getBody();
    assertThat(body).hasSize(10);
    assertThat(body.get(0).studyId()).isEqualTo("study-10");
    assertThat(body.get(9).studyId()).isEqualTo("study-19");
  }

  // --------------------------------------------------------------------------
  // Last partial page
  // --------------------------------------------------------------------------

  /**
   * When the page overlaps the end of the list (items 20–24 with pageSize=10), only the remaining 5
   * items must be returned — no {@link IndexOutOfBoundsException}.
   */
  @Test
  void getAllStudies_lastPartialPage_returnsRemainingStudies() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, 10, 3, Direction.ASC);

    List<CancerStudyMetadataDTO> body = response.getBody();
    assertThat(body).hasSize(5);
    assertThat(body.get(0).studyId()).isEqualTo("study-20");
    assertThat(body.get(4).studyId()).isEqualTo("study-24");
  }

  // --------------------------------------------------------------------------
  // Page beyond end
  // --------------------------------------------------------------------------

  /** A {@code pageNumber} beyond the available data must return an empty list, not throw. */
  @Test
  void getAllStudies_pageNumberBeyondEnd_returnsEmptyList() {
    ResponseEntity<List<CancerStudyMetadataDTO>> response =
        controller.getAllStudies(null, ProjectionType.SUMMARY, null, 10, 4, Direction.ASC);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  // --------------------------------------------------------------------------
  // META projection — total count header
  // --------------------------------------------------------------------------

  /**
   * When {@code projection=META} is combined with {@code pageSize} / {@code pageNumber}, the {@code
   * X-Total-Count} and {@code total-count} headers must reflect the <em>total</em> number of
   * authorized studies — not the number of items on the current page.
   *
   * <p>Prior to the fix, {@code studies.size()} was read <em>after</em> the in-memory subList was
   * applied, so the header reported the page size (10) instead of the full count (25).
   */
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
