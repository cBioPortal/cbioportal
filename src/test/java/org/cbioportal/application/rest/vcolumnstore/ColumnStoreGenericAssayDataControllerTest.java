package org.cbioportal.application.rest.vcolumnstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.cbioportal.domain.generic_assay.usecase.FetchGenericAssayDataUseCase;
import org.cbioportal.legacy.model.GenericAssayData;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.utils.Encoder;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.GenericAssayFilter;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
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
@ContextConfiguration(classes = {ColumnStoreGenericAssayDataController.class, TestConfig.class})
public class ColumnStoreGenericAssayDataControllerTest {

  private static final String URL = "/api/generic_assay_data/prof/fetch";

  @MockitoBean private FetchGenericAssayDataUseCase fetchGenericAssayDataUseCase;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @WithMockUser
  public void fetchKeepsTheLegacyResponseShape() throws Exception {
    when(fetchGenericAssayDataUseCase.execute(any(), any(), any(), any()))
        .thenReturn(List.of(data("0.5", true)));

    GenericAssayFilter filter = new GenericAssayFilter();
    filter.setSampleListId("list");
    filter.setGenericAssayStableIds(List.of("entity"));

    mockMvc
        .perform(post(URL, filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value("prof"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value("s1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value("p1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("study"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].genericAssayStableId").value("entity"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").value("entity"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value("0.5"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientLevel").value(true))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey")
                .value(Encoder.calculateBase64("s1", "study")))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey")
                .value(Encoder.calculateBase64("p1", "study")));

    verify(fetchGenericAssayDataUseCase)
        .execute(eq("prof"), eq("list"), eq(null), eq(List.of("entity")));
  }

  @Test
  @WithMockUser
  public void sampleIdsArePassedOnAndMetaReturnsTheCountHeader() throws Exception {
    when(fetchGenericAssayDataUseCase.execute(any(), any(), any(), any()))
        .thenReturn(List.of(data("1", false), data("2", false)));

    GenericAssayFilter filter = new GenericAssayFilter();
    filter.setSampleIds(List.of("s1"));

    mockMvc
        .perform(post(URL + "?projection=META", filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));

    verify(fetchGenericAssayDataUseCase).execute(eq("prof"), eq(null), eq(List.of("s1")), eq(null));
  }

  @Test
  @WithMockUser
  public void unknownOrWrongTypeProfileIsNotFound() throws Exception {
    when(fetchGenericAssayDataUseCase.execute(any(), any(), any(), any()))
        .thenThrow(new MolecularProfileNotFoundException("prof"));

    GenericAssayFilter filter = new GenericAssayFilter();
    filter.setSampleListId("list");

    mockMvc.perform(post(URL, filter)).andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  @Test
  @WithMockUser
  public void filterNeedsExactlyOneOfSampleIdsAndSampleList() throws Exception {
    mockMvc
        .perform(post(URL, new GenericAssayFilter()))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    GenericAssayFilter both = new GenericAssayFilter();
    both.setSampleListId("list");
    both.setSampleIds(List.of("s1"));
    mockMvc.perform(post(URL, both)).andExpect(MockMvcResultMatchers.status().isBadRequest());

    verifyNoInteractions(fetchGenericAssayDataUseCase);
  }

  private RequestBuilder post(String url, GenericAssayFilter filter) throws Exception {
    return MockMvcRequestBuilders.post(url)
        .with(csrf())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(filter));
  }

  private GenericAssayData data(String value, boolean patientLevel) {
    GenericAssayData data = new GenericAssayData();
    data.setMolecularProfileId("prof");
    data.setSampleId("s1");
    data.setPatientId("p1");
    data.setStudyId("study");
    data.setGenericAssayStableId("entity");
    data.setValue(value);
    data.setPatientLevel(patientLevel);
    return data;
  }
}
