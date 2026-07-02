package org.cbioportal.application.rest.vcolumnstore;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableResult;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;
import org.cbioportal.domain.resource.usecase.GetResourceTableDataUseCase;
import org.cbioportal.domain.resource.usecase.GetResourceTableTabsUseCase;
import org.cbioportal.legacy.web.config.TestConfig;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ResourceTableController.class, TestConfig.class})
public class ResourceTableControllerTest {

  private static final String STUDY_ID = "study_tcga_pub";
  private static final String RESOURCE_ID = "HE_SLIDE";

  @MockitoBean private GetResourceTableTabsUseCase getResourceTableTabsUseCase;
  @MockitoBean private GetResourceTableDataUseCase getResourceTableDataUseCase;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  // ---- /tabs/fetch ----

  @Test
  @WithMockUser
  public void fetchResourceTableTabs_returnsTabList() throws Exception {
    List<ResourceTableTab> tabs =
        List.of(
            new ResourceTableTab(RESOURCE_ID, "H&E Slide", 10L, 8L, 10L),
            new ResourceTableTab("CT_SCAN", "CT Scan", 5L, 5L, 0L));

    Mockito.when(getResourceTableTabsUseCase.execute(Mockito.any())).thenReturn(tabs);

    ResourceTabsRequest request = new ResourceTabsRequest(List.of(STUDY_ID), null, null);

    MvcResult mvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/resource-table/tabs/fetch")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(MockMvcResultMatchers.request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].resourceId").value(RESOURCE_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].label").value("H&E Slide"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalCount").value(10))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientCount").value(8))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleCount").value(10))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].resourceId").value("CT_SCAN"));
  }

  @Test
  @WithMockUser
  public void fetchResourceTableTabs_emptyBody_returnsEmptyList() throws Exception {
    Mockito.when(getResourceTableTabsUseCase.execute(Mockito.any())).thenReturn(List.of());

    MvcResult mvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/resource-table/tabs/fetch")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(MockMvcResultMatchers.request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));
  }

  // ---- /query/fetch ----

  @Test
  @WithMockUser
  public void fetchResourceTableData_returnsResultWithRows() throws Exception {
    List<ResourceTableRow> rows =
        List.of(
            new ResourceTableRow(
                STUDY_ID,
                RESOURCE_ID,
                "H&E Slide",
                "SAMPLE",
                "tcga-a1-a0sb",
                "tcga-a1-a0sb-01",
                "https://example.com/he1.jpg",
                "H&E Sample 1",
                "IMAGE",
                1,
                Map.of("stain", "HE")),
            new ResourceTableRow(
                STUDY_ID,
                RESOURCE_ID,
                "H&E Slide",
                "SAMPLE",
                "tcga-a1-a0sd",
                "tcga-a1-a0sd-01",
                "https://example.com/he2.jpg",
                "H&E Sample 2",
                "IMAGE",
                1,
                Map.of("stain", "HE")));

    ResourceTableResult result =
        new ResourceTableResult(List.of(), List.of(), rows, 2L, 2L, 2L, Map.of());

    Mockito.when(getResourceTableDataUseCase.execute(Mockito.any())).thenReturn(result);

    ResourceTableQuery query =
        new ResourceTableQuery(
            List.of(STUDY_ID), RESOURCE_ID, null, null, null, 0, 10, null, null, null);

    MvcResult mvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/resource-table/query/fetch")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(query)))
            .andExpect(MockMvcResultMatchers.request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$.totalRowCount").value(2))
        .andExpect(MockMvcResultMatchers.jsonPath("$.filteredPatientCount").value(2))
        .andExpect(MockMvcResultMatchers.jsonPath("$.filteredSampleCount").value(2))
        .andExpect(MockMvcResultMatchers.jsonPath("$.rows", Matchers.hasSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.rows[0].resourceId").value(RESOURCE_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$.rows[0].patientId").value("tcga-a1-a0sb"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.rows[0].sampleId").value("tcga-a1-a0sb-01"))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.rows[0].url").value("https://example.com/he1.jpg"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.rows[0].type").value("IMAGE"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.rows[0].metadata.stain").value("HE"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.rows[1].patientId").value("tcga-a1-a0sd"));
  }

  @Test
  @WithMockUser
  public void fetchResourceTableData_nullQuery_returnsEmptyResult() throws Exception {
    ResourceTableResult emptyResult =
        new ResourceTableResult(List.of(), List.of(), List.of(), 0L, 0L, 0L, Map.of());

    Mockito.when(getResourceTableDataUseCase.execute(Mockito.any())).thenReturn(emptyResult);

    MvcResult mvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/resource-table/query/fetch")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(MockMvcResultMatchers.request().asyncStarted())
            .andReturn();

    mockMvc
        .perform(MockMvcRequestBuilders.asyncDispatch(mvcResult))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.totalRowCount").value(0))
        .andExpect(MockMvcResultMatchers.jsonPath("$.rows", Matchers.hasSize(0)));
  }
}
