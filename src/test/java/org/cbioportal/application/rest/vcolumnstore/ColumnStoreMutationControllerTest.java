package org.cbioportal.application.rest.vcolumnstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.cbioportal.domain.mutation.usecase.FetchAllMutationsInProfileUseCase;
import org.cbioportal.domain.mutation.usecase.FetchMetaMutationsUseCase;
import org.cbioportal.domain.mutation.usecase.FetchMutationsInSingleProfileUseCase;
import org.cbioportal.domain.mutation.usecase.MutationUseCases;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.utils.Encoder;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.MutationFilter;
import org.cbioportal.legacy.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.shared.MutationQueryOptions;
import org.cbioportal.shared.enums.ProjectionType;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
@ContextConfiguration(classes = {ColumnStoreMutationController.class, TestConfig.class})
public class ColumnStoreMutationControllerTest {

  private static final String PROFILE_URL = "/api/molecular-profiles/prof/mutations/fetch";
  private static final String MULTI_URL = "/api/mutations/fetch";

  @MockitoBean private MutationUseCases mutationUseCases;

  private final FetchMutationsInSingleProfileUseCase singleProfileUseCase =
      org.mockito.Mockito.mock(FetchMutationsInSingleProfileUseCase.class);
  private final FetchAllMutationsInProfileUseCase multiProfileUseCase =
      org.mockito.Mockito.mock(FetchAllMutationsInProfileUseCase.class);
  private final FetchMetaMutationsUseCase metaUseCase =
      org.mockito.Mockito.mock(FetchMetaMutationsUseCase.class);

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void setUp() {
    when(mutationUseCases.fetchMutationsInSingleProfileUseCase()).thenReturn(singleProfileUseCase);
    when(mutationUseCases.fetchAllMutationsInProfileUseCase()).thenReturn(multiProfileUseCase);
    when(mutationUseCases.fetchMetaMutationsUseCase()).thenReturn(metaUseCase);
  }

  @Test
  @WithMockUser
  public void perProfileFetchBySampleListKeepsTheLegacyResponseShape() throws Exception {
    when(singleProfileUseCase.execute(any(), any(), any(), any(), any()))
        .thenReturn(List.of(mutation()));

    MutationFilter filter = new MutationFilter();
    filter.setSampleListId("list");
    filter.setEntrezGeneIds(List.of(7157));

    mockMvc
        .perform(post(PROFILE_URL + "?projection=DETAILED", filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value("prof"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value("s1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(7157))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinChange").value("R175H"))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey")
                .value(Encoder.calculateBase64("s1", "study")))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey")
                .value(Encoder.calculateBase64("p1", "study")));

    ArgumentCaptor<MutationQueryOptions> options =
        ArgumentCaptor.forClass(MutationQueryOptions.class);
    verify(singleProfileUseCase)
        .execute(eq("prof"), eq("list"), eq(null), eq(List.of(7157)), options.capture());
    org.junit.Assert.assertEquals(ProjectionType.DETAILED, options.getValue().projection());
  }

  @Test
  @WithMockUser
  public void perProfileFetchBySampleIdsPassesPagingAndSorting() throws Exception {
    when(singleProfileUseCase.execute(any(), any(), any(), any(), any())).thenReturn(List.of());

    MutationFilter filter = new MutationFilter();
    filter.setSampleIds(List.of("s1", "s2"));

    mockMvc
        .perform(
            post(
                PROFILE_URL + "?pageSize=5&pageNumber=2&sortBy=proteinChange&direction=DESC",
                filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));

    ArgumentCaptor<MutationQueryOptions> options =
        ArgumentCaptor.forClass(MutationQueryOptions.class);
    verify(singleProfileUseCase)
        .execute(eq("prof"), eq(null), eq(List.of("s1", "s2")), eq(null), options.capture());
    org.junit.Assert.assertEquals(5, options.getValue().pageSize().intValue());
    org.junit.Assert.assertEquals(2, options.getValue().pageNumber().intValue());
    org.junit.Assert.assertEquals("DESC", options.getValue().direction().name());
  }

  @Test
  @WithMockUser
  public void perProfileMetaProjectionReturnsCountHeaders() throws Exception {
    when(singleProfileUseCase.executeMeta(any(), any(), any(), any())).thenReturn(meta(12, 5));

    MutationFilter filter = new MutationFilter();
    filter.setSampleIds(List.of("s1"));

    mockMvc
        .perform(post(PROFILE_URL + "?projection=META", filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "12"))
        .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.SAMPLE_COUNT, "5"));
  }

  @Test
  @WithMockUser
  public void perProfileUnknownOrNonMutationProfileIsNotFound() throws Exception {
    when(singleProfileUseCase.execute(any(), any(), any(), any(), any()))
        .thenThrow(new MolecularProfileNotFoundException("prof"));

    MutationFilter filter = new MutationFilter();
    filter.setSampleIds(List.of("s1"));

    mockMvc
        .perform(post(PROFILE_URL, filter))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  @Test
  @WithMockUser
  public void perProfileFilterNeedsExactlyOneOfSampleIdsAndSampleList() throws Exception {
    mockMvc
        .perform(post(PROFILE_URL, new MutationFilter()))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    MutationFilter both = new MutationFilter();
    both.setSampleIds(List.of("s1"));
    both.setSampleListId("list");
    mockMvc
        .perform(post(PROFILE_URL, both))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    verifyNoInteractions(singleProfileUseCase);
  }

  @Test
  @WithMockUser
  public void multiProfileFetchByProfileIds() throws Exception {
    when(multiProfileUseCase.execute(any(), any())).thenReturn(List.of(mutation()));

    MutationMultipleStudyFilter filter = new MutationMultipleStudyFilter();
    filter.setMolecularProfileIds(List.of("prof_a", "prof_b"));
    filter.setEntrezGeneIds(List.of(7157));

    mockMvc
        .perform(postMulti(MULTI_URL + "?projection=DETAILED", filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].proteinChange").value("R175H"));

    verify(multiProfileUseCase).execute(any(MutationMultipleStudyFilter.class), any());
  }

  @Test
  @WithMockUser
  public void multiProfileMetaProjectionReturnsCountHeaders() throws Exception {
    when(metaUseCase.execute(any())).thenReturn(meta(7, 3));

    MutationMultipleStudyFilter filter = new MutationMultipleStudyFilter();
    SampleMolecularIdentifier identifier = new SampleMolecularIdentifier();
    identifier.setMolecularProfileId("prof_a");
    identifier.setSampleId("s1");
    filter.setSampleMolecularIdentifiers(List.of(identifier));

    mockMvc
        .perform(postMulti(MULTI_URL + "?projection=META", filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "7"))
        .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.SAMPLE_COUNT, "3"));

    verifyNoInteractions(multiProfileUseCase);
  }

  @Test
  @WithMockUser
  public void multiProfileFilterWithBothOrNeitherIdentifierKindIsRejected() throws Exception {
    mockMvc
        .perform(postMulti(MULTI_URL, new MutationMultipleStudyFilter()))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    MutationMultipleStudyFilter both = new MutationMultipleStudyFilter();
    both.setMolecularProfileIds(List.of("prof_a"));
    both.setSampleMolecularIdentifiers(List.of(new SampleMolecularIdentifier()));
    mockMvc
        .perform(postMulti(MULTI_URL, both))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    verifyNoInteractions(multiProfileUseCase);
  }

  private RequestBuilder post(String url, MutationFilter filter) throws Exception {
    return json(url, objectMapper.writeValueAsString(filter));
  }

  private RequestBuilder postMulti(String url, MutationMultipleStudyFilter filter)
      throws Exception {
    return json(url, objectMapper.writeValueAsString(filter));
  }

  private RequestBuilder json(String url, String body) {
    return MockMvcRequestBuilders.post(url)
        .with(csrf())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body);
  }

  private Mutation mutation() {
    Mutation mutation = new Mutation();
    mutation.setMolecularProfileId("prof");
    mutation.setSampleId("s1");
    mutation.setPatientId("p1");
    mutation.setStudyId("study");
    mutation.setEntrezGeneId(7157);
    mutation.setProteinChange("R175H");
    return mutation;
  }

  private MutationMeta meta(int total, int samples) {
    MutationMeta meta = new MutationMeta();
    meta.setTotalCount(total);
    meta.setSampleCount(samples);
    return meta;
  }
}
