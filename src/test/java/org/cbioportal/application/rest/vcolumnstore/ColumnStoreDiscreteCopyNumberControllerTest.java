package org.cbioportal.application.rest.vcolumnstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.cbioportal.domain.discrete_copy_number.usecase.FetchDiscreteCopyNumbersUseCase;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.utils.Encoder;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.DiscreteCopyNumberFilter;
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
@ContextConfiguration(classes = {ColumnStoreDiscreteCopyNumberController.class, TestConfig.class})
public class ColumnStoreDiscreteCopyNumberControllerTest {

  private static final String URL = "/api/molecular-profiles/prof/discrete-copy-number/fetch";

  @MockitoBean private FetchDiscreteCopyNumbersUseCase fetchDiscreteCopyNumbersUseCase;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @WithMockUser
  public void fetchKeepsTheLegacyResponseShape() throws Exception {
    when(fetchDiscreteCopyNumbersUseCase.execute(any(), any(), any(), any(), any(), anyBoolean()))
        .thenReturn(List.of(data()));

    mockMvc
        .perform(post(URL + "?projection=DETAILED", sampleListFilter()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value("prof"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value("s1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value("p1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("study"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(7157))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].alteration").value(-2))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.hugoGeneSymbol").value("TP53"))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey")
                .value(Encoder.calculateBase64("s1", "study")))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey")
                .value(Encoder.calculateBase64("p1", "study")));

    // default event type is HOMDEL_AND_AMP
    verify(fetchDiscreteCopyNumbersUseCase)
        .execute(eq("prof"), eq("list"), eq(null), eq(List.of(7157)), eq(List.of(-2, 2)), eq(true));
  }

  @Test
  @WithMockUser
  public void eventTypeAndSampleIdsArePassedOn() throws Exception {
    when(fetchDiscreteCopyNumbersUseCase.execute(any(), any(), any(), any(), any(), anyBoolean()))
        .thenReturn(List.of());

    DiscreteCopyNumberFilter filter = new DiscreteCopyNumberFilter();
    filter.setSampleIds(List.of("s1"));

    mockMvc
        .perform(post(URL + "?discreteCopyNumberEventType=ALL", filter))
        .andExpect(MockMvcResultMatchers.status().isOk());

    verify(fetchDiscreteCopyNumbersUseCase)
        .execute(
            eq("prof"),
            eq(null),
            eq(List.of("s1")),
            eq(null),
            eq(List.of(-2, -1, 0, 1, 2)),
            eq(false));
  }

  @Test
  @WithMockUser
  public void metaProjectionReturnsTheTotalCountHeader() throws Exception {
    when(fetchDiscreteCopyNumbersUseCase.count(any(), any(), any(), any(), any())).thenReturn(9);

    mockMvc
        .perform(post(URL + "?projection=META", sampleListFilter()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "9"));
  }

  @Test
  @WithMockUser
  public void unknownOrWrongTypeProfileIsNotFound() throws Exception {
    when(fetchDiscreteCopyNumbersUseCase.execute(any(), any(), any(), any(), any(), anyBoolean()))
        .thenThrow(new MolecularProfileNotFoundException("prof"));

    mockMvc
        .perform(post(URL, sampleListFilter()))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  @Test
  @WithMockUser
  public void filterNeedsExactlyOneOfSampleIdsAndSampleList() throws Exception {
    mockMvc
        .perform(post(URL, new DiscreteCopyNumberFilter()))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    DiscreteCopyNumberFilter both = sampleListFilter();
    both.setSampleIds(List.of("s1"));
    mockMvc.perform(post(URL, both)).andExpect(MockMvcResultMatchers.status().isBadRequest());

    verifyNoInteractions(fetchDiscreteCopyNumbersUseCase);
  }

  private RequestBuilder post(String url, DiscreteCopyNumberFilter filter) throws Exception {
    return MockMvcRequestBuilders.post(url)
        .with(csrf())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(filter));
  }

  private DiscreteCopyNumberFilter sampleListFilter() {
    DiscreteCopyNumberFilter filter = new DiscreteCopyNumberFilter();
    filter.setSampleListId("list");
    filter.setEntrezGeneIds(List.of(7157));
    return filter;
  }

  private DiscreteCopyNumberData data() {
    Gene gene = new Gene();
    gene.setEntrezGeneId(7157);
    gene.setHugoGeneSymbol("TP53");
    gene.setType("protein-coding");
    DiscreteCopyNumberData data = new DiscreteCopyNumberData();
    data.setMolecularProfileId("prof");
    data.setSampleId("s1");
    data.setPatientId("p1");
    data.setStudyId("study");
    data.setEntrezGeneId(7157);
    data.setAlteration(-2);
    data.setGene(gene);
    return data;
  }
}
