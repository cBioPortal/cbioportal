package org.cbioportal.legacy.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.legacy.model.ResourceData;
import org.cbioportal.legacy.service.ResourceDataService;
import org.cbioportal.legacy.web.config.TestConfig;
import org.hamcrest.Matchers;
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

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ResourceDataController.class, TestConfig.class})
public class ResourceDataControllerTest {

  @MockitoBean private ResourceDataService resourceDataService;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Autowired private MockMvc mockMvc;

  @Test
  @WithMockUser
  public void getAllResourceDataOfSampleInStudy() throws Exception {

    String resourceDatasJson =
        "["
            + "  {"
            + "    \"sampleId\": \"TCGA-OR-A5J1-01\","
            + "    \"patientId\": \"TCGA-OR-A5J1\","
            + "    \"studyId\": \"test_study_id\","
            + "    \"resourceId\": \"PATHOLOGY_SLIDE\","
            + "    \"url\": \"http://url-to-slide-sample1\""
            + "  }"
            + "]";

    List<ResourceData> resourceDataList =
        Arrays.asList(objectMapper.readValue(resourceDatasJson, ResourceData[].class));

    Mockito.when(
            resourceDataService.getAllResourceDataOfSampleInStudy(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()))
        .thenReturn(resourceDataList);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    "/api/studies/test_study_id/samples/test_sample_id/resource-data")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value("TCGA-OR-A5J1-01"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value("TCGA-OR-A5J1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("test_study_id"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].resourceId").value("PATHOLOGY_SLIDE"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].url").value("http://url-to-slide-sample1"));
  }

  @Test
  @WithMockUser
  public void getAllResourceDataOfPatientInStudy() throws Exception {

    String resourceDatasJson =
        "["
            + "  {"
            + "    \"patientId\": \"TCGA-OR-A5J1\","
            + "    \"studyId\": \"test_study_id\","
            + "    \"resourceId\": \"PATIENT_NOTES\","
            + "    \"url\": \"http://url-to-notes-patient1.txt\""
            + "  }"
            + "]";

    List<ResourceData> resourceDataList =
        Arrays.asList(objectMapper.readValue(resourceDatasJson, ResourceData[].class));

    Mockito.when(
            resourceDataService.getAllResourceDataOfPatientInStudy(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()))
        .thenReturn(resourceDataList);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    "/api/studies/test_study_id/patients/test_sample_id/resource-data")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value("TCGA-OR-A5J1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("test_study_id"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].resourceId").value("PATIENT_NOTES"))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].url").value("http://url-to-notes-patient1.txt"));
  }

  @Test
  @WithMockUser
  public void getAllClinicalDataInStudy() throws Exception {

    String resourceDatasJson =
        "["
            + "  {"
            + "    \"studyId\": \"test_study_id\","
            + "    \"resourceId\": \"STUDY_SPONSORS\","
            + "    \"url\": \"http://url-to-sponsors.txt\""
            + "  }"
            + "]";

    List<ResourceData> resourceDataList =
        Arrays.asList(objectMapper.readValue(resourceDatasJson, ResourceData[].class));

    Mockito.when(
            resourceDataService.getAllResourceDataForStudy(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()))
        .thenReturn(resourceDataList);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/studies/test_study_id/resource-data")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("test_study_id"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].resourceId").value("STUDY_SPONSORS"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].url").value("http://url-to-sponsors.txt"));
  }

  // ---- URL templates ----

  private static final String TEMPLATE =
      "${base_url}/wsi/patient/${PATIENT_ID}?studyId=${STUDY_ID}&slideKey=${metadata.slide_key}";

  private static ResourceData row(String patientId, String sampleId, String url, String metadata) {
    ResourceData row = new ResourceData();
    row.setStudyId("mskimpact");
    row.setResourceId("WSI");
    row.setPatientId(patientId);
    row.setSampleId(sampleId);
    row.setUrl(url);
    row.setUrlTemplate(TEMPLATE);
    row.setMetadata(metadata);
    return row;
  }

  private static List<ResourceData> templatedRows() {
    ResourceData noTemplate = row("P-3", null, "", null);
    noTemplate.setUrlTemplate(null);
    return List.of(
        row("P-1", "P-1-T01", "", "{\"slide_key\":\"k 1\"}"),
        row("P-2", null, "https://example.com/own.svs", "{\"slide_key\":\"k2\"}"),
        noTemplate);
  }

  private void expectTemplatedUrls(String path, String baseUrl) throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(baseUrl + path)
                .contextPath(baseUrl.endsWith("/cbioportal") ? "/cbioportal" : "")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(3)))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].url")
                .value(baseUrl + "/wsi/patient/P-1?studyId=mskimpact&slideKey=k%201"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].url").value("https://example.com/own.svs"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[2].url").value(""))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].urlTemplate").doesNotExist())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].metadata").doesNotExist());
  }

  @Test
  @WithMockUser
  public void sampleResourceData_expandsUrlTemplates() throws Exception {
    Mockito.when(
            resourceDataService.getAllResourceDataOfSampleInStudy(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()))
        .thenReturn(templatedRows());

    expectTemplatedUrls("/api/studies/mskimpact/samples/P-1-T01/resource-data", "http://localhost");
  }

  @Test
  @WithMockUser
  public void patientResourceData_expandsUrlTemplatesUnderAContextPath() throws Exception {
    Mockito.when(
            resourceDataService.getAllResourceDataOfPatientInStudy(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()))
        .thenReturn(templatedRows());

    expectTemplatedUrls(
        "/api/studies/mskimpact/patients/P-1/resource-data",
        "http://portal.example.org/cbioportal");
  }

  @Test
  @WithMockUser
  public void studyResourceData_expandsUrlTemplates() throws Exception {
    Mockito.when(
            resourceDataService.getAllResourceDataForStudy(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()))
        .thenReturn(templatedRows());

    expectTemplatedUrls("/api/studies/mskimpact/resource-data", "http://localhost");
  }

  @Test
  @WithMockUser
  public void allResourceDataOfAStudy_expandsUrlTemplates() throws Exception {
    Mockito.when(
            resourceDataService.getAllResourceDataForStudyPatientSample(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()))
        .thenReturn(templatedRows());

    expectTemplatedUrls("/api/studies/mskimpact/resource-data-all", "http://localhost");
  }

  @Test
  @WithMockUser
  public void expansionDoesNotModifyTheServiceRows() throws Exception {
    List<ResourceData> rows = templatedRows();
    Mockito.when(
            resourceDataService.getAllResourceDataForStudy(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()))
        .thenReturn(rows);

    expectTemplatedUrls("/api/studies/mskimpact/resource-data", "http://localhost");

    // The study-wide rows are cached, so the request-specific expansion must not be written back.
    assertThat(rows.get(0).getUrl()).isEmpty();
  }
}
