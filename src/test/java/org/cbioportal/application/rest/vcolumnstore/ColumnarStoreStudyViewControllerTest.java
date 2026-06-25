package org.cbioportal.application.rest.vcolumnstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.cbioportal.domain.studyview.StudyViewService;
import org.cbioportal.legacy.model.GenericAssayDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.web.parameter.GenericAssayDataCountFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ColumnarStoreStudyViewControllerTest {

  private static final String TEST_STUDY_ID = "test_study_id";
  private static final String TEST_STABLE_ID = "test_stable_id";
  private static final String TEST_GENERIC_ASSAY_DATA_VALUE_1 = "value1";
  private static final String TEST_GENERIC_ASSAY_DATA_VALUE_2 = "value2";
  private static final String TEST_MOLECULAR_PROFILE_TYPE = "test_molecular_profile_type";

  private StudyViewService studyViewService;
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void setUp() {
    studyViewService = Mockito.mock(StudyViewService.class);
    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new ColumnarStoreStudyViewController(
                    studyViewService, null, null, null, null, null, null))
            .build();
  }

  @Test
  public void fetchGenericAssayDataCounts() throws Exception {
    List<GenericAssayDataCountItem> genericAssayDataCountItems =
        List.of(
            new GenericAssayDataCountItem(
                TEST_STABLE_ID,
                List.of(
                    new GenericAssayDataCount(TEST_GENERIC_ASSAY_DATA_VALUE_1, 3),
                    new GenericAssayDataCount(TEST_GENERIC_ASSAY_DATA_VALUE_2, 1))));

    Mockito.when(
            studyViewService.getGenericAssayDataCounts(
                Mockito.any(StudyViewFilter.class), Mockito.<List<GenericAssayDataFilter>>any()))
        .thenReturn(genericAssayDataCountItems);

    GenericAssayDataCountFilter genericAssayDataCountFilter = new GenericAssayDataCountFilter();
    genericAssayDataCountFilter.setGenericAssayDataFilters(
        List.of(new GenericAssayDataFilter(TEST_STABLE_ID, TEST_MOLECULAR_PROFILE_TYPE)));
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(TEST_STUDY_ID));
    genericAssayDataCountFilter.setStudyViewFilter(studyViewFilter);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/generic-assay-data-counts/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genericAssayDataCountFilter)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").value(TEST_STABLE_ID))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].counts[0].value")
                .value(TEST_GENERIC_ASSAY_DATA_VALUE_1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].count").value(3))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].counts[1].value")
                .value(TEST_GENERIC_ASSAY_DATA_VALUE_2))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].count").value(1));

    Mockito.verify(studyViewService)
        .getGenericAssayDataCounts(
            Mockito.any(StudyViewFilter.class), Mockito.<List<GenericAssayDataFilter>>any());
    Mockito.verify(studyViewService, Mockito.never())
        .getGenericAssayDataCounts(Mockito.any(StudyViewFilter.class), Mockito.anyString());
  }

  @Test
  public void fetchGenericAssayDataCountsByProfileType() throws Exception {
    List<GenericAssayDataCountItem> genericAssayDataCountItems =
        List.of(
            new GenericAssayDataCountItem(
                TEST_STABLE_ID,
                List.of(new GenericAssayDataCount(TEST_GENERIC_ASSAY_DATA_VALUE_1, 3))));

    Mockito.when(
            studyViewService.getGenericAssayDataCounts(
                Mockito.any(StudyViewFilter.class), Mockito.eq(TEST_MOLECULAR_PROFILE_TYPE)))
        .thenReturn(genericAssayDataCountItems);

    GenericAssayDataCountFilter genericAssayDataCountFilter = new GenericAssayDataCountFilter();
    genericAssayDataCountFilter.setProfileType(TEST_MOLECULAR_PROFILE_TYPE);
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(TEST_STUDY_ID));
    genericAssayDataCountFilter.setStudyViewFilter(studyViewFilter);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/generic-assay-data-counts/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genericAssayDataCountFilter)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").value(TEST_STABLE_ID))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].counts[0].value")
                .value(TEST_GENERIC_ASSAY_DATA_VALUE_1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].count").value(3));

    Mockito.verify(studyViewService)
        .getGenericAssayDataCounts(
            Mockito.any(StudyViewFilter.class), Mockito.eq(TEST_MOLECULAR_PROFILE_TYPE));
    Mockito.verify(studyViewService, Mockito.never())
        .getGenericAssayDataCounts(
            Mockito.any(StudyViewFilter.class), Mockito.<List<GenericAssayDataFilter>>any());
  }

  @Test
  public void fetchGenericAssayDataCounts_missingFiltersAndProfileType_returnsBadRequest()
      throws Exception {
    GenericAssayDataCountFilter genericAssayDataCountFilter = new GenericAssayDataCountFilter();
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(TEST_STUDY_ID));
    genericAssayDataCountFilter.setStudyViewFilter(studyViewFilter);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/generic-assay-data-counts/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genericAssayDataCountFilter)))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    Mockito.verifyNoInteractions(studyViewService);
  }

  @Test
  public void fetchGenericAssayDataCounts_blankProfileType_returnsBadRequest() throws Exception {
    GenericAssayDataCountFilter genericAssayDataCountFilter = new GenericAssayDataCountFilter();
    genericAssayDataCountFilter.setProfileType("   ");
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(TEST_STUDY_ID));
    genericAssayDataCountFilter.setStudyViewFilter(studyViewFilter);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/generic-assay-data-counts/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genericAssayDataCountFilter)))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    Mockito.verifyNoInteractions(studyViewService);
  }
}
