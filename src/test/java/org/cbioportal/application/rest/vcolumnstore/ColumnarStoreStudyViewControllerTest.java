package org.cbioportal.application.rest.vcolumnstore;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.cbioportal.domain.studyview.StudyViewService;
import org.cbioportal.infrastructure.service.BasicDataBinner;
import org.cbioportal.infrastructure.service.ClinicalDataBinner;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.GenericAssayDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.service.ClinicalDataDensityPlotService;
import org.cbioportal.legacy.service.CustomDataService;
import org.cbioportal.legacy.service.ViolinPlotService;
import org.cbioportal.legacy.web.columnar.util.CustomDataFilterUtil;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.GenericAssayDataCountFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
@ContextConfiguration(classes = {ColumnarStoreStudyViewController.class, TestConfig.class})
public class ColumnarStoreStudyViewControllerTest {

  private static final String TEST_STUDY_ID = "test_study_id";
  private static final String TEST_STABLE_ID = "test_stable_id";
  private static final String TEST_GENERIC_ASSAY_DATA_VALUE_1 = "value1";
  private static final String TEST_GENERIC_ASSAY_DATA_VALUE_2 = "value2";
  private static final String TEST_MOLECULAR_PROFILE_TYPE = "test_molecular_profile_type";

  @MockitoBean private StudyViewService studyViewService;
  @MockitoBean private BasicDataBinner basicDataBinner;
  @MockitoBean private ClinicalDataBinner clinicalDataBinner;
  @MockitoBean private ClinicalDataDensityPlotService clinicalDataDensityPlotService;
  @MockitoBean private ViolinPlotService violinPlotService;
  @MockitoBean private CustomDataService customDataService;
  @MockitoBean private CustomDataFilterUtil customDataFilterUtil;

  @MockitoBean(name = "staticRefCacheMapUtil")
  private CacheMapUtil cacheMapUtil;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void setUp() {
    CancerStudy cancerStudy = new CancerStudy();
    cancerStudy.setCancerStudyIdentifier(TEST_STUDY_ID);
    cancerStudy.setPublicStudy(true);

    Mockito.when(cacheMapUtil.getCancerStudyMap()).thenReturn(Map.of(TEST_STUDY_ID, cancerStudy));
    Mockito.when(cacheMapUtil.hasCacheEnabled()).thenReturn(false);
  }

  @Test
  @WithMockUser
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
            MockMvcRequestBuilders.post("/api/column-store/generic-assay-data-counts/fetch")
                .with(csrf())
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
  @WithMockUser
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
            MockMvcRequestBuilders.post("/api/column-store/generic-assay-data-counts/fetch")
                .with(csrf())
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
  @WithMockUser
  public void fetchGenericAssayDataCounts_missingFiltersAndProfileType_returnsBadRequest()
      throws Exception {
    GenericAssayDataCountFilter genericAssayDataCountFilter = new GenericAssayDataCountFilter();
    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(TEST_STUDY_ID));
    genericAssayDataCountFilter.setStudyViewFilter(studyViewFilter);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/column-store/generic-assay-data-counts/fetch")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genericAssayDataCountFilter)))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    Mockito.verifyNoInteractions(studyViewService);
  }
}
