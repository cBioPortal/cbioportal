package org.cbioportal.application.rest.vcolumnstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.cbioportal.domain.gene_panel_data.usecase.FetchGenePanelDataUseCase;
import org.cbioportal.legacy.model.GenePanelData;
import org.cbioportal.legacy.utils.Encoder;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.GenePanelDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ColumnStoreGenePanelDataController.class, TestConfig.class})
public class ColumnStoreGenePanelDataControllerTest {

  private static final String URL = "/api/gene-panel-data/fetch";

  @MockitoBean private FetchGenePanelDataUseCase fetchGenePanelDataUseCase;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @WithMockUser
  public void fetchKeepsTheLegacyResponseShape() throws Exception {
    when(fetchGenePanelDataUseCase.execute(any()))
        .thenReturn(List.of(data("panel1", true), data(null, false)));

    GenePanelDataMultipleStudyFilter filter = new GenePanelDataMultipleStudyFilter();
    filter.setMolecularProfileIds(List.of("prof"));

    mockMvc
        .perform(post(filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value("prof"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value("s1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value("p1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("study"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].genePanelId").value("panel1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].profiled").value(true))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey")
                .value(Encoder.calculateBase64("s1", "study")))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey")
                .value(Encoder.calculateBase64("p1", "study")))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].genePanelId").doesNotExist())
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].profiled").value(false));

    verify(fetchGenePanelDataUseCase).execute(any(GenePanelDataMultipleStudyFilter.class));
  }

  @Test
  @WithMockUser
  public void fetchAcceptsSampleMolecularIdentifiers() throws Exception {
    when(fetchGenePanelDataUseCase.execute(any())).thenReturn(List.of());

    SampleMolecularIdentifier identifier = new SampleMolecularIdentifier();
    identifier.setMolecularProfileId("prof");
    identifier.setSampleId("s1");
    GenePanelDataMultipleStudyFilter filter = new GenePanelDataMultipleStudyFilter();
    filter.setSampleMolecularIdentifiers(List.of(identifier));

    mockMvc
        .perform(post(filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));
  }

  @Test
  @WithMockUser
  public void filterWithBothOrNeitherIdentifierKindIsRejected() throws Exception {
    mockMvc
        .perform(post(new GenePanelDataMultipleStudyFilter()))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    GenePanelDataMultipleStudyFilter both = new GenePanelDataMultipleStudyFilter();
    both.setMolecularProfileIds(List.of("prof"));
    both.setSampleMolecularIdentifiers(List.of(new SampleMolecularIdentifier()));
    mockMvc.perform(post(both)).andExpect(MockMvcResultMatchers.status().isBadRequest());

    verifyNoInteractions(fetchGenePanelDataUseCase);
  }

  private org.springframework.test.web.servlet.RequestBuilder post(
      GenePanelDataMultipleStudyFilter filter) throws Exception {
    return MockMvcRequestBuilders.post(URL)
        .with(csrf())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(filter));
  }

  private GenePanelData data(String panel, boolean profiled) {
    GenePanelData data = new GenePanelData();
    data.setMolecularProfileId("prof");
    data.setSampleId("s1");
    data.setPatientId("p1");
    data.setStudyId("study");
    data.setGenePanelId(panel);
    data.setProfiled(profiled);
    return data;
  }
}
