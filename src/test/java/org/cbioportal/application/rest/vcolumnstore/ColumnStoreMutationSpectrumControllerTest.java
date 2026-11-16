package org.cbioportal.application.rest.vcolumnstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.cbioportal.domain.mutation.usecase.FetchMutationSpectrumsUseCase;
import org.cbioportal.legacy.model.MutationSpectrum;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.utils.Encoder;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.MutationSpectrumFilter;
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
@ContextConfiguration(classes = {ColumnStoreMutationSpectrumController.class, TestConfig.class})
public class ColumnStoreMutationSpectrumControllerTest {

  private static final String URL = "/api/molecular-profiles/prof/mutation-spectrums/fetch";

  @MockitoBean private FetchMutationSpectrumsUseCase fetchMutationSpectrumsUseCase;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @WithMockUser
  public void fetchKeepsTheLegacyResponseShape() throws Exception {
    when(fetchMutationSpectrumsUseCase.execute(any(), any(), any()))
        .thenReturn(List.of(spectrum()));

    MutationSpectrumFilter filter = new MutationSpectrumFilter();
    filter.setSampleListId("list");

    mockMvc
        .perform(post(filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value("prof"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value("s1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value("p1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("study"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].CtoA").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].CtoG").value(2))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].CtoT").value(3))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].TtoA").value(4))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].TtoC").value(5))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].TtoG").value(6))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey")
                .value(Encoder.calculateBase64("s1", "study")));

    verify(fetchMutationSpectrumsUseCase).execute(eq("prof"), eq("list"), eq(null));
  }

  @Test
  @WithMockUser
  public void sampleIdsArePassedOn() throws Exception {
    when(fetchMutationSpectrumsUseCase.execute(any(), any(), any())).thenReturn(List.of());

    MutationSpectrumFilter filter = new MutationSpectrumFilter();
    filter.setSampleIds(List.of("s1", "s2"));

    mockMvc.perform(post(filter)).andExpect(MockMvcResultMatchers.status().isOk());

    verify(fetchMutationSpectrumsUseCase).execute(eq("prof"), eq(null), eq(List.of("s1", "s2")));
  }

  @Test
  @WithMockUser
  public void unknownOrNonMutationProfileIsNotFound() throws Exception {
    when(fetchMutationSpectrumsUseCase.execute(any(), any(), any()))
        .thenThrow(new MolecularProfileNotFoundException("prof"));

    MutationSpectrumFilter filter = new MutationSpectrumFilter();
    filter.setSampleListId("list");

    mockMvc.perform(post(filter)).andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  @Test
  @WithMockUser
  public void filterNeedsExactlyOneOfSampleIdsAndSampleList() throws Exception {
    mockMvc
        .perform(post(new MutationSpectrumFilter()))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    MutationSpectrumFilter both = new MutationSpectrumFilter();
    both.setSampleListId("list");
    both.setSampleIds(List.of("s1"));
    mockMvc.perform(post(both)).andExpect(MockMvcResultMatchers.status().isBadRequest());

    verifyNoInteractions(fetchMutationSpectrumsUseCase);
  }

  private RequestBuilder post(MutationSpectrumFilter filter) throws Exception {
    return MockMvcRequestBuilders.post(URL)
        .with(csrf())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(filter));
  }

  private MutationSpectrum spectrum() {
    MutationSpectrum spectrum = new MutationSpectrum();
    spectrum.setMolecularProfileId("prof");
    spectrum.setSampleId("s1");
    spectrum.setPatientId("p1");
    spectrum.setStudyId("study");
    spectrum.setCtoA(1);
    spectrum.setCtoG(2);
    spectrum.setCtoT(3);
    spectrum.setTtoA(4);
    spectrum.setTtoC(5);
    spectrum.setTtoG(6);
    return spectrum;
  }
}
