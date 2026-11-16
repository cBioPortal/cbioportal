package org.cbioportal.application.rest.vcolumnstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.cbioportal.domain.geneset_data.usecase.FetchGenesetDataUseCase;
import org.cbioportal.legacy.model.GenesetMolecularData;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;
import org.cbioportal.legacy.utils.Encoder;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.GenesetDataFilterCriteria;
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
@ContextConfiguration(classes = {ColumnStoreGenesetDataController.class, TestConfig.class})
public class ColumnStoreGenesetDataControllerTest {

  private static final String URL = "/api/genetic-profiles/prof/geneset-genetic-data/fetch";

  @MockitoBean private FetchGenesetDataUseCase fetchGenesetDataUseCase;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @WithMockUser
  public void fetchBySampleListKeepsTheLegacyResponseShape() throws Exception {
    when(fetchGenesetDataUseCase.execute(any(), any(), any(), any())).thenReturn(List.of(data()));

    GenesetDataFilterCriteria criteria = new GenesetDataFilterCriteria();
    criteria.setSampleListId("list");
    criteria.setGenesetIds(List.of("set1"));

    mockMvc
        .perform(post(criteria))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].geneticProfileId").value("prof"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value("s1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value("p1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("study"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].genesetId").value("set1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value("0.5"))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey")
                .value(Encoder.calculateBase64("s1", "study")));

    verify(fetchGenesetDataUseCase).execute(eq("prof"), eq("list"), eq(null), eq(List.of("set1")));
  }

  @Test
  @WithMockUser
  public void blankSampleListFallsBackToSampleIds() throws Exception {
    when(fetchGenesetDataUseCase.execute(any(), any(), any(), any())).thenReturn(List.of());

    GenesetDataFilterCriteria criteria = new GenesetDataFilterCriteria();
    criteria.setSampleListId("  ");
    criteria.setSampleIds(List.of("s1"));
    criteria.setGenesetIds(List.of("set1"));

    mockMvc.perform(post(criteria)).andExpect(MockMvcResultMatchers.status().isOk());

    verify(fetchGenesetDataUseCase)
        .execute(eq("prof"), eq(null), eq(List.of("s1")), eq(List.of("set1")));
  }

  @Test
  @WithMockUser
  public void unknownProfileOrSampleListIsNotFound() throws Exception {
    GenesetDataFilterCriteria criteria = new GenesetDataFilterCriteria();
    criteria.setSampleListId("list");

    doThrow(new MolecularProfileNotFoundException("prof"))
        .when(fetchGenesetDataUseCase)
        .execute(any(), any(), any(), any());
    mockMvc.perform(post(criteria)).andExpect(MockMvcResultMatchers.status().isNotFound());

    doThrow(new SampleListNotFoundException("list"))
        .when(fetchGenesetDataUseCase)
        .execute(any(), any(), any(), any());
    mockMvc.perform(post(criteria)).andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  private RequestBuilder post(GenesetDataFilterCriteria criteria) throws Exception {
    return MockMvcRequestBuilders.post(URL)
        .with(csrf())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(criteria));
  }

  private GenesetMolecularData data() {
    GenesetMolecularData data = new GenesetMolecularData();
    data.setMolecularProfileId("prof");
    data.setSampleId("s1");
    data.setPatientId("p1");
    data.setStudyId("study");
    data.setGenesetId("set1");
    data.setValue("0.5");
    return data;
  }
}
