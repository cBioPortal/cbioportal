package org.cbioportal.application.rest.vcolumnstore;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.cbioportal.domain.resource.usecase.GetResourceTableDataUseCase;
import org.cbioportal.domain.resource.usecase.GetResourceTableMetadataUseCase;
import org.cbioportal.domain.resource.usecase.GetResourceTableTabsUseCase;
import org.cbioportal.legacy.web.config.TestConfig;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * Rows served by {@code /resource-table/query/fetch} without a URL of their own carry their
 * definition's URL template, expanded against the base URL of the request that asked for them. Runs
 * the real use case over a mocked repository.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(
    classes = {ResourceTableController.class, GetResourceTableDataUseCase.class, TestConfig.class})
public class ResourceTableControllerUrlTemplateTest {

  private static final String STUDY_ID = "mskimpact";
  private static final String RESOURCE_ID = "WSI_SAMPLE";

  @MockitoBean private GetResourceTableTabsUseCase getResourceTableTabsUseCase;
  @MockitoBean private GetResourceTableMetadataUseCase getResourceTableMetadataUseCase;
  @MockitoBean private ResourceDataRepository resourceDataRepository;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void rows() {
    Mockito.when(resourceDataRepository.getResourceTableRows(Mockito.any()))
        .thenReturn(
            List.of(
                new ResourceTableRow(
                    STUDY_ID,
                    RESOURCE_ID,
                    "Slides",
                    "SAMPLE",
                    "P-0000001",
                    "P-0000001-T01-IM3",
                    "",
                    "H&E",
                    Map.of("slide_key", "2351e12d4955")),
                new ResourceTableRow(
                    STUDY_ID,
                    RESOURCE_ID,
                    "Slides",
                    "SAMPLE",
                    "P-0000002",
                    "P-0000002-T01-IM3",
                    "https://example.com/own.svs",
                    "H&E",
                    Map.of("slide_key", "b3286836fc27"))));
    Mockito.when(resourceDataRepository.getResourceUrlTemplates(Mockito.any()))
        .thenReturn(
            Map.of(
                STUDY_ID,
                "${base_url}/wsi/patient/${PATIENT_ID}?studyId=${STUDY_ID}"
                    + "&slideKey=${metadata.slide_key}"));
  }

  private String query() throws Exception {
    return objectMapper.writeValueAsString(
        new ResourceTableQuery(
            List.of(STUDY_ID), RESOURCE_ID, null, null, null, 0, 10, null, null, null));
  }

  @Test
  @WithMockUser
  public void expandsTheTemplateAgainstTheRequestBaseUrl() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/resource-table/query/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(query()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].url")
                .value(
                    "http://localhost/wsi/patient/P-0000001?studyId=mskimpact"
                        + "&slideKey=2351e12d4955"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].url").value("https://example.com/own.svs"));
  }

  @Test
  @WithMockUser
  public void baseUrlIncludesTheContextPath() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(
                    "http://portal.example.org/cbioportal/api/resource-table/query/fetch")
                .contextPath("/cbioportal")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(query()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].url")
                .value(
                    "http://portal.example.org/cbioportal/wsi/patient/P-0000001"
                        + "?studyId=mskimpact&slideKey=2351e12d4955"));
  }
}
