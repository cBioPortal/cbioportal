package org.cbioportal.application.rest.vcolumnstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.clinical_data.ClinicalDataType;
import org.cbioportal.domain.clinical_data.usecase.FetchClinicalDataMetaUseCase;
import org.cbioportal.domain.clinical_data.usecase.FetchClinicalDataUseCase;
import org.cbioportal.legacy.utils.Encoder;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.shared.enums.ProjectionType;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ColumnStoreClinicalDataController.class, TestConfig.class})
public class ColumnStoreClinicalDataControllerTest {

  private static final String URL = "/api/clinical-data/fetch";

  @MockitoBean private FetchClinicalDataUseCase fetchClinicalDataUseCase;
  @MockitoBean private FetchClinicalDataMetaUseCase fetchClinicalDataMetaUseCase;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @WithMockUser
  public void fetchSampleDataKeepsTheLegacyResponseShape() throws Exception {
    when(fetchClinicalDataUseCase.execute(any(), any(), any()))
        .thenReturn(List.of(new ClinicalData(1, "s1", "p1", "study", "CANCER_TYPE", "Breast")));

    mockMvc
        .perform(post(URL, filter()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey")
                .value(Encoder.calculateBase64("s1", "study")))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey")
                .value(Encoder.calculateBase64("p1", "study")))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value("s1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value("p1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("study"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttributeId").value("CANCER_TYPE"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value("Breast"));

    verify(fetchClinicalDataUseCase)
        .execute(any(), eq(ClinicalDataType.SAMPLE), eq(ProjectionType.SUMMARY));
  }

  @Test
  @WithMockUser
  public void fetchPatientDataHasNoSampleKey() throws Exception {
    when(fetchClinicalDataUseCase.execute(any(), any(), any()))
        .thenReturn(List.of(new ClinicalData(1, null, "p1", "study", "OS_MONTHS", "12.5")));

    mockMvc
        .perform(post(URL + "?clinicalDataType=PATIENT", filter()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey").doesNotExist())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").doesNotExist())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey")
                .value(Encoder.calculateBase64("p1", "study")));

    verify(fetchClinicalDataUseCase)
        .execute(any(), eq(ClinicalDataType.PATIENT), eq(ProjectionType.SUMMARY));
  }

  @Test
  @WithMockUser
  public void fetchMetaProjectionReturnsTotalCountHeader() throws Exception {
    when(fetchClinicalDataMetaUseCase.execute(any(), any())).thenReturn(42);

    mockMvc
        .perform(post(URL + "?projection=META", filter()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "42"));

    verifyNoInteractions(fetchClinicalDataUseCase);
  }

  @Test
  @WithMockUser
  public void invalidFiltersAreRejected() throws Exception {
    ClinicalDataMultiStudyFilter noAttributes = filter();
    noAttributes.setAttributeIds(List.of());
    mockMvc
        .perform(post(URL, noAttributes))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    ClinicalDataMultiStudyFilter noIdentifiers = filter();
    noIdentifiers.setIdentifiers(List.of());
    mockMvc
        .perform(post(URL, noIdentifiers))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    verifyNoInteractions(fetchClinicalDataUseCase);
  }

  private RequestBuilder post(String url, ClinicalDataMultiStudyFilter filter) throws Exception {
    return MockMvcRequestBuilders.post(url)
        .with(csrf())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(filter));
  }

  private ClinicalDataMultiStudyFilter filter() {
    ClinicalDataIdentifier identifier = new ClinicalDataIdentifier();
    identifier.setEntityId("s1");
    identifier.setStudyId("study");
    ClinicalDataMultiStudyFilter filter = new ClinicalDataMultiStudyFilter();
    filter.setIdentifiers(List.of(identifier));
    filter.setAttributeIds(List.of("CANCER_TYPE"));
    return filter;
  }
}
