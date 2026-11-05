package org.cbioportal.application.rest.vcolumnstore;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceScope;
import org.cbioportal.domain.resource.ResourceScopeRow;
import org.cbioportal.domain.resource.ResourceTableCounts;
import org.cbioportal.domain.resource.ResourceTableGroup;
import org.cbioportal.domain.resource.ResourceTableGroupPage;
import org.cbioportal.domain.resource.ResourceTableMetadataView;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.cbioportal.domain.resource.usecase.GetResourceTableDataUseCase;
import org.cbioportal.domain.resource.usecase.GetResourceTableGroupsUseCase;
import org.cbioportal.domain.resource.usecase.GetResourceTableMetadataUseCase;
import org.cbioportal.domain.resource.usecase.GetResourceTableTabsUseCase;
import org.cbioportal.domain.studyview.StudyViewService;
import org.cbioportal.legacy.web.config.TestConfig;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Request validation for the grouped resource table, through the real use cases with the repository
 * mocked: what a client gets back for a request the endpoints cannot answer as asked.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(
    classes = {
      ResourceTableController.class,
      GetResourceTableTabsUseCase.class,
      GetResourceTableDataUseCase.class,
      GetResourceTableMetadataUseCase.class,
      GetResourceTableGroupsUseCase.class,
      TestConfig.class
    })
public class ResourceTableGroupsControllerTest {

  private static final String STUDY_ID = "study_genie_pub";
  private static final String GROUPS = "/api/resource-table/groups/fetch";
  private static final String ROWS = "/api/resource-table/query/fetch";
  private static final String METADATA = "/api/resource-table/metadata/fetch";

  @MockitoBean private ResourceDataRepository resourceDataRepository;
  @MockitoBean private StudyViewService studyViewService;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void knownResources() {
    Mockito.when(resourceDataRepository.getResourceScope(Mockito.any()))
        .thenReturn(
            new ResourceScope(
                List.of(
                    new ResourceScopeRow("WSI_SAMPLE", STUDY_ID, "", true, true),
                    new ResourceScopeRow("WSI_PATIENT", STUDY_ID, "", true, true))));
    Mockito.when(resourceDataRepository.getResourceTableGroups(Mockito.any(), Mockito.any()))
        .thenReturn(
            new ResourceTableGroupPage(
                2L,
                5L,
                1L,
                List.of(
                    new ResourceTableGroup(
                        STUDY_ID,
                        "P-1",
                        null,
                        3L,
                        Map.of("metadata:stain_group", Map.of("H&E", 2L, "IHC", 1L))))));
    Mockito.when(resourceDataRepository.getResourceTableCounts(Mockito.any(), Mockito.any()))
        .thenReturn(ResourceTableCounts.empty());
    Mockito.when(resourceDataRepository.getResourceTableMetadata(Mockito.any(), Mockito.any()))
        .thenReturn(ResourceTableMetadataView.empty());
    Mockito.when(resourceDataRepository.getResourceTableRows(Mockito.any(), Mockito.any()))
        .thenReturn(List.of());
  }

  private static Map<String, Object> request() {
    Map<String, Object> body = new HashMap<>();
    body.put("studyIds", List.of(STUDY_ID));
    body.put("resourceIds", List.of("WSI_SAMPLE", "WSI_PATIENT"));
    body.put("groupBy", "patient");
    body.put("groupCounts", List.of("metadata:stain_group"));
    body.put("pageNumber", 0);
    body.put("pageSize", 50);
    return body;
  }

  private ResultActions post(String path, Map<String, Object> body) throws Exception {
    return mockMvc.perform(
        MockMvcRequestBuilders.post(path)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)));
  }

  private void expectBadRequest(String path, Map<String, Object> body, String messagePart)
      throws Exception {
    post(path, body)
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString(messagePart)));
  }

  @Test
  @WithMockUser
  public void groups_returnsTotalsLocatedIndexAndGroupCounts() throws Exception {
    Map<String, Object> body = request();
    body.put("locate", Map.of("studyId", STUDY_ID, "patientId", "P-1"));

    post(GROUPS, body)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.totalGroups").value(2))
        .andExpect(MockMvcResultMatchers.jsonPath("$.totalRows").value(5))
        .andExpect(MockMvcResultMatchers.jsonPath("$.locatedIndex").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$.groups[0].patientId").value("P-1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.groups[0].sampleId").doesNotExist())
        .andExpect(MockMvcResultMatchers.jsonPath("$.groups[0].rowCount").value(3))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.groups[0].counts['metadata:stain_group']['H&E']")
                .value(2));
  }

  @Test
  @WithMockUser
  public void groups_badGroupBy_isABadRequest() throws Exception {
    Map<String, Object> body = request();
    body.put("groupBy", "slide");
    expectBadRequest(GROUPS, body, "groupBy must be 'patient' or 'sample'");
  }

  @Test
  @WithMockUser
  public void groups_missingGroupBy_isABadRequest() throws Exception {
    Map<String, Object> body = request();
    body.remove("groupBy");
    expectBadRequest(GROUPS, body, "groupBy is required");
  }

  @Test
  @WithMockUser
  public void groups_unknownResource_isABadRequest() throws Exception {
    Map<String, Object> body = request();
    body.put("resourceIds", List.of("WSI_SAMPLE", "NOT_A_RESOURCE"));
    expectBadRequest(GROUPS, body, "Unknown resource [NOT_A_RESOURCE]");
  }

  @Test
  @WithMockUser
  public void groups_pageSizeOutOfBounds_isABadRequest() throws Exception {
    Map<String, Object> body = request();
    body.put("pageSize", 0);
    expectBadRequest(GROUPS, body, "pageSize must be between 1 and 1000");
    body.put("pageSize", GetResourceTableGroupsUseCase.MAX_PAGE_SIZE + 1);
    expectBadRequest(GROUPS, body, "pageSize must be between 1 and 1000");
    body.put("pageSize", GetResourceTableGroupsUseCase.MAX_PAGE_SIZE);
    post(GROUPS, body).andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  @WithMockUser
  public void groups_negativePageNumber_isABadRequest() throws Exception {
    Map<String, Object> body = request();
    body.put("pageNumber", -1);
    expectBadRequest(GROUPS, body, "pageNumber must not be negative");
  }

  @Test
  @WithMockUser
  public void groups_offsetBeyondAnInt_isABadRequest() throws Exception {
    Map<String, Object> body = request();
    body.put("pageNumber", Integer.MAX_VALUE);
    body.put("pageSize", GetResourceTableGroupsUseCase.MAX_PAGE_SIZE);
    expectBadRequest(GROUPS, body, "pageNumber * pageSize must not exceed 2147483647");
    // The last page whose offset still fits is answered.
    body.put("pageNumber", Integer.MAX_VALUE / GetResourceTableGroupsUseCase.MAX_PAGE_SIZE);
    post(GROUPS, body).andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  @WithMockUser
  public void groups_sortThatIsNotAGroupSort_isABadRequest() throws Exception {
    Map<String, Object> body = request();
    body.put("sortBy", "metadata:stain_group");
    expectBadRequest(GROUPS, body, "Groups sort by patientId, studyId or rowCount");
    body.put("sortBy", "sampleId");
    expectBadRequest(GROUPS, body, "not 'sampleId'");
  }

  @Test
  @WithMockUser
  public void groups_groupCountsColumnThatIsNotCountable_isABadRequest() throws Exception {
    Map<String, Object> body = request();
    body.put("groupCounts", List.of("url"));
    expectBadRequest(GROUPS, body, "groupCounts entries must be");
  }

  @Test
  @WithMockUser
  public void groups_locateWithoutTheGroupKey_isABadRequest() throws Exception {
    Map<String, Object> body = request();
    body.put("groupBy", "sample");
    body.put("locate", Map.of("studyId", STUDY_ID, "patientId", "P-1"));
    expectBadRequest(GROUPS, body, "locate needs a studyId and a sampleId");
  }

  @Test
  @WithMockUser
  public void rowQuery_withGroupBy_isABadRequest() throws Exception {
    expectBadRequest(ROWS, request(), "/resource-table/groups/fetch");
  }

  @Test
  @WithMockUser
  public void metadata_badGroupBy_isABadRequest() throws Exception {
    Map<String, Object> body = request();
    body.put("groupBy", "slide");
    expectBadRequest(METADATA, body, "groupBy must be 'patient' or 'sample', not 'slide'");
  }

  @Test
  @WithMockUser
  public void rowQuery_unknownResourceInResourceIds_isABadRequest() throws Exception {
    Map<String, Object> body = request();
    body.remove("groupBy");
    body.remove("groupCounts");
    body.put("resourceIds", List.of("NOT_A_RESOURCE"));
    expectBadRequest("/api/resource-table/query/fetch", body, "Unknown resource");
  }

  @Test
  @WithMockUser
  public void rowQuery_singleResourceId_isNotValidatedAgainstTheScope() throws Exception {
    // A single resourceId keeps its behaviour: an unknown one reads no rows rather than erroring.
    Mockito.when(resourceDataRepository.getResourceTableRows(Mockito.any(), Mockito.any()))
        .thenReturn(List.of());
    Map<String, Object> body = new HashMap<>();
    body.put("studyIds", List.of(STUDY_ID));
    body.put("resourceId", "NOT_A_RESOURCE");
    body.put("pageSize", 10);

    post("/api/resource-table/query/fetch", body)
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));
    Mockito.verify(resourceDataRepository, Mockito.never()).getResourceScope(Mockito.any());
  }

  @Test
  @WithMockUser
  public void groups_nullBody_isABadRequest() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(GROUPS)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(
            MockMvcResultMatchers.jsonPath(
                "$.message", Matchers.containsString("groupBy is required")));
  }

  @Test
  @WithMockUser
  public void groupBy_isParsedIgnoringCase() throws Exception {
    Map<String, Object> body = request();
    body.put("groupBy", "PATIENT");
    post(GROUPS, body).andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  @WithMockUser
  public void invalidStudyViewFilter_isABadRequestOnEveryEndpoint() throws Exception {
    // A study-view filter names its cohort by studyIds or by sampleIdentifiers, not both.
    Map<String, Object> body = request();
    body.put(
        "studyViewFilter",
        Map.of(
            "studyIds",
            List.of(STUDY_ID),
            "sampleIdentifiers",
            List.of(Map.of("studyId", STUDY_ID, "sampleId", "S-1"))));
    post(GROUPS, body).andExpect(MockMvcResultMatchers.status().isBadRequest());
    post(METADATA, body).andExpect(MockMvcResultMatchers.status().isBadRequest());
    body.remove("groupBy");
    body.remove("groupCounts");
    post(ROWS, body).andExpect(MockMvcResultMatchers.status().isBadRequest());
    Mockito.verify(studyViewService, Mockito.never()).buildStudyViewFilterContext(Mockito.any());
  }

  @Test
  @WithMockUser
  public void groupCountsOrLocateWithoutGroupBy_isABadRequest() throws Exception {
    for (String path : List.of(ROWS, METADATA)) {
      Map<String, Object> body = request();
      body.remove("groupBy");
      expectBadRequest(path, body, "groupCounts and locate apply only to a grouped query");
      body.remove("groupCounts");
      body.put("locate", Map.of("studyId", STUDY_ID, "patientId", "P-1"));
      expectBadRequest(path, body, "groupCounts and locate apply only to a grouped query");
    }
  }

  @Test
  @WithMockUser
  public void metadata_boundsGroupCountsLikeTheGroupsEndpoint() throws Exception {
    Map<String, Object> body = request();
    post(METADATA, body).andExpect(MockMvcResultMatchers.status().isOk());
    body.put(
        "groupCounts",
        java.util.stream.IntStream.rangeClosed(1, 11).mapToObj(i -> "metadata:k" + i).toList());
    expectBadRequest(METADATA, body, "groupCounts may name at most 10 columns");
    expectBadRequest(GROUPS, body, "groupCounts may name at most 10 columns");
  }
}
