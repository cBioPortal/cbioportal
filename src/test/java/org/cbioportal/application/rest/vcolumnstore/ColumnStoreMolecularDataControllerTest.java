package org.cbioportal.application.rest.vcolumnstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.domain.genomic_data.usecase.FetchGeneMolecularDataUseCase;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.utils.Encoder;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.MolecularDataFilter;
import org.cbioportal.legacy.web.parameter.MolecularDataMultipleStudyFilter;
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
@ContextConfiguration(classes = {ColumnStoreMolecularDataController.class, TestConfig.class})
public class ColumnStoreMolecularDataControllerTest {

  private static final String PROFILE_ID = "test_molecular_profile_id";
  private static final String FETCH_URL =
      "/api/molecular-profiles/" + PROFILE_ID + "/molecular-data/fetch";
  private static final String SAMPLE_LIST_ID = "test_sample_list_id";

  @MockitoBean private FetchGeneMolecularDataUseCase fetchGeneMolecularDataUseCase;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @WithMockUser
  public void fetchBySampleIdsSummaryProjection() throws Exception {
    when(fetchGeneMolecularDataUseCase.executeBySampleIds(any(), any(), any(), anyBoolean()))
        .thenReturn(exampleMolecularData(false));

    mockMvc
        .perform(post(FETCH_URL, sampleIdsFilter()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value(PROFILE_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value("sample_1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value("patient_1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("test_study"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(2.3))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey")
                .value(Encoder.calculateBase64("sample_1", "test_study")))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey")
                .value(Encoder.calculateBase64("patient_1", "test_study")))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value("sample_2"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(2))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(-0.0004));

    verify(fetchGeneMolecularDataUseCase)
        .executeBySampleIds(
            eq(PROFILE_ID), eq(List.of("sample_1", "sample_2")), eq(List.of(1, 2)), eq(false));
  }

  @Test
  @WithMockUser
  public void fetchBySampleListId() throws Exception {
    when(fetchGeneMolecularDataUseCase.executeBySampleList(any(), any(), any(), anyBoolean()))
        .thenReturn(exampleMolecularData(false));

    MolecularDataFilter filter = new MolecularDataFilter();
    filter.setSampleListId(SAMPLE_LIST_ID);
    filter.setEntrezGeneIds(List.of(1, 2));

    mockMvc
        .perform(post(FETCH_URL, filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)));

    verify(fetchGeneMolecularDataUseCase)
        .executeBySampleList(eq(PROFILE_ID), eq(SAMPLE_LIST_ID), eq(List.of(1, 2)), eq(false));
  }

  @Test
  @WithMockUser
  public void fetchDetailedProjectionIncludesGene() throws Exception {
    when(fetchGeneMolecularDataUseCase.executeBySampleIds(any(), any(), any(), anyBoolean()))
        .thenReturn(exampleMolecularData(true));

    mockMvc
        .perform(post(FETCH_URL + "?projection=DETAILED", sampleIdsFilter()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.entrezGeneId").value(1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.hugoGeneSymbol").value("GENE1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.type").value("protein-coding"));

    verify(fetchGeneMolecularDataUseCase)
        .executeBySampleIds(eq(PROFILE_ID), any(), any(), eq(true));
  }

  @Test
  @WithMockUser
  public void fetchMetaProjectionReturnsTotalCountHeader() throws Exception {
    when(fetchGeneMolecularDataUseCase.executeBySampleIds(any(), any(), any(), anyBoolean()))
        .thenReturn(exampleMolecularData(false));

    mockMvc
        .perform(post(FETCH_URL + "?projection=META", sampleIdsFilter()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"))
        .andExpect(MockMvcResultMatchers.content().string(""));
  }

  @Test
  @WithMockUser
  public void fetchDropsNonNumericValues() throws Exception {
    List<GeneMolecularData> data = exampleMolecularData(false);
    data.add(molecularData("sample_3", 1, "NA", false));
    data.add(molecularData("sample_4", 1, null, false));
    data.add(molecularData("sample_5", 1, "NaN", false));
    when(fetchGeneMolecularDataUseCase.executeBySampleIds(any(), any(), any(), anyBoolean()))
        .thenReturn(data);

    mockMvc
        .perform(post(FETCH_URL, sampleIdsFilter()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)));
  }

  @Test
  @WithMockUser
  public void fetchUnknownOrMutationProfileReturnsNotFound() throws Exception {
    when(fetchGeneMolecularDataUseCase.executeBySampleIds(any(), any(), any(), anyBoolean()))
        .thenThrow(new MolecularProfileNotFoundException(PROFILE_ID));

    mockMvc
        .perform(post(FETCH_URL, sampleIdsFilter()))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  @Test
  @WithMockUser
  public void fetchWithoutSampleIdsAndSampleListIdIsRejected() throws Exception {
    MolecularDataFilter filter = new MolecularDataFilter();
    filter.setEntrezGeneIds(List.of(1));

    mockMvc
        .perform(post(FETCH_URL, filter))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    verifyNoInteractions(fetchGeneMolecularDataUseCase);
  }

  @Test
  @WithMockUser
  public void fetchWithBothSampleIdsAndSampleListIdIsRejected() throws Exception {
    MolecularDataFilter filter = sampleIdsFilter();
    filter.setSampleListId(SAMPLE_LIST_ID);

    mockMvc
        .perform(post(FETCH_URL, filter))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    verifyNoInteractions(fetchGeneMolecularDataUseCase);
  }

  @Test
  @WithMockUser
  public void multiProfileFetchByProfileIds() throws Exception {
    when(fetchGeneMolecularDataUseCase.executeInMultipleProfiles(any(), any(), any(), anyBoolean()))
        .thenReturn(exampleMolecularData(false));

    MolecularDataMultipleStudyFilter filter = new MolecularDataMultipleStudyFilter();
    filter.setMolecularProfileIds(List.of("profile_a", "profile_b"));
    filter.setEntrezGeneIds(List.of(1, 2));

    mockMvc
        .perform(postMulti("/api/molecular-data/fetch", filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value("sample_1"))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey")
                .value(Encoder.calculateBase64("sample_1", "test_study")))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene").doesNotExist());

    verify(fetchGeneMolecularDataUseCase)
        .executeInMultipleProfiles(
            eq(List.of("profile_a", "profile_b")), eq(null), eq(List.of(1, 2)), eq(false));
  }

  @Test
  @WithMockUser
  public void multiProfileFetchBySampleMolecularIdentifiers() throws Exception {
    when(fetchGeneMolecularDataUseCase.executeInMultipleProfiles(any(), any(), any(), anyBoolean()))
        .thenReturn(exampleMolecularData(true));

    SampleMolecularIdentifier first = new SampleMolecularIdentifier();
    first.setMolecularProfileId("profile_a");
    first.setSampleId("sample_1");
    SampleMolecularIdentifier second = new SampleMolecularIdentifier();
    second.setMolecularProfileId("profile_b");
    second.setSampleId("sample_2");
    MolecularDataMultipleStudyFilter filter = new MolecularDataMultipleStudyFilter();
    filter.setSampleMolecularIdentifiers(List.of(first, second));
    filter.setEntrezGeneIds(List.of(1));

    mockMvc
        .perform(postMulti("/api/molecular-data/fetch?projection=DETAILED", filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].gene.hugoGeneSymbol").value("GENE1"));

    verify(fetchGeneMolecularDataUseCase)
        .executeInMultipleProfiles(
            eq(List.of("profile_a", "profile_b")),
            eq(List.of("sample_1", "sample_2")),
            eq(List.of(1)),
            eq(true));
  }

  @Test
  @WithMockUser
  public void multiProfileFetchMetaProjectionReturnsTotalCountHeader() throws Exception {
    when(fetchGeneMolecularDataUseCase.executeInMultipleProfiles(any(), any(), any(), anyBoolean()))
        .thenReturn(exampleMolecularData(false));

    MolecularDataMultipleStudyFilter filter = new MolecularDataMultipleStudyFilter();
    filter.setMolecularProfileIds(List.of("profile_a"));
    filter.setEntrezGeneIds(List.of(1));

    mockMvc
        .perform(postMulti("/api/molecular-data/fetch?projection=META", filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
  }

  @Test
  @WithMockUser
  public void multiProfileFetchWithBothOrNeitherIdentifierKindIsRejected() throws Exception {
    MolecularDataMultipleStudyFilter neither = new MolecularDataMultipleStudyFilter();
    neither.setEntrezGeneIds(List.of(1));
    mockMvc
        .perform(postMulti("/api/molecular-data/fetch", neither))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    MolecularDataMultipleStudyFilter both = new MolecularDataMultipleStudyFilter();
    both.setMolecularProfileIds(List.of("profile_a"));
    both.setSampleMolecularIdentifiers(List.of(new SampleMolecularIdentifier()));
    both.setEntrezGeneIds(List.of(1));
    mockMvc
        .perform(postMulti("/api/molecular-data/fetch", both))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    verifyNoInteractions(fetchGeneMolecularDataUseCase);
  }

  private org.springframework.test.web.servlet.RequestBuilder postMulti(
      String url, MolecularDataMultipleStudyFilter filter) throws Exception {
    return MockMvcRequestBuilders.post(url)
        .with(csrf())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(filter));
  }

  private org.springframework.test.web.servlet.RequestBuilder post(
      String url, MolecularDataFilter filter) throws Exception {
    return MockMvcRequestBuilders.post(url)
        .with(csrf())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(filter));
  }

  private MolecularDataFilter sampleIdsFilter() {
    MolecularDataFilter filter = new MolecularDataFilter();
    filter.setSampleIds(List.of("sample_1", "sample_2"));
    filter.setEntrezGeneIds(List.of(1, 2));
    return filter;
  }

  private List<GeneMolecularData> exampleMolecularData(boolean withGene) {
    List<GeneMolecularData> data = new ArrayList<>();
    data.add(molecularData("sample_1", 1, "2.3", withGene));
    data.add(molecularData("sample_2", 2, "-4.0E-4", withGene));
    return data;
  }

  private GeneMolecularData molecularData(
      String sampleId, int entrezGeneId, String value, boolean withGene) {
    GeneMolecularData data = new GeneMolecularData();
    data.setMolecularProfileId(PROFILE_ID);
    data.setSampleId(sampleId);
    data.setPatientId(sampleId.replace("sample", "patient"));
    data.setStudyId("test_study");
    data.setEntrezGeneId(entrezGeneId);
    data.setValue(value);
    if (withGene) {
      Gene gene = new Gene();
      gene.setEntrezGeneId(entrezGeneId);
      gene.setHugoGeneSymbol("GENE" + entrezGeneId);
      gene.setType("protein-coding");
      data.setGene(gene);
    }
    return data;
  }
}
