package org.cbioportal.application.rest.vcolumnstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.studyview.StudyViewService;
import org.cbioportal.infrastructure.service.BasicDataBinner;
import org.cbioportal.legacy.model.ClinicalDataBin;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.legacy.model.GenericAssayDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.service.CustomDataService;
import org.cbioportal.legacy.web.columnar.util.CustomDataFilterUtil;
import org.cbioportal.legacy.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataBinFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataFilter;
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
  private BasicDataBinner basicDataBinner;
  private CustomDataService customDataService;
  private CustomDataFilterUtil customDataFilterUtil;
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void setUp() {
    studyViewService = Mockito.mock(StudyViewService.class);
    basicDataBinner = Mockito.mock(BasicDataBinner.class);
    customDataService = Mockito.mock(CustomDataService.class);
    customDataFilterUtil = Mockito.mock(CustomDataFilterUtil.class);

    mockMvc =
        MockMvcBuilders.standaloneSetup(
                new ColumnarStoreStudyViewController(
                    studyViewService,
                    basicDataBinner,
                    null,
                    null,
                    null,
                    customDataService,
                    customDataFilterUtil))
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

  @Test
  public void fetchCustomDataCounts() throws Exception {
    ClinicalDataCount count = new ClinicalDataCount();
    count.setAttributeId(TEST_STABLE_ID);
    count.setValue("value1");
    count.setCount(3);

    ClinicalDataCountItem countItem = new ClinicalDataCountItem();
    countItem.setAttributeId(TEST_STABLE_ID);
    countItem.setCounts(List.of(count));

    Mockito.when(studyViewService.getFilteredSamples(Mockito.any(StudyViewFilter.class)))
        .thenReturn(List.of(new Sample(1, TEST_STABLE_ID, "patient1", TEST_STUDY_ID)));

    Mockito.when(customDataService.getCustomDataSessions(Mockito.anyList()))
        .thenReturn(Collections.emptyMap());

    Mockito.when(customDataFilterUtil.getCustomDataCounts(Mockito.anyList(), Mockito.anyMap()))
        .thenReturn(List.of(countItem));

    ClinicalDataFilter clinicalDataFilter = new ClinicalDataFilter();
    clinicalDataFilter.setAttributeId(TEST_STABLE_ID);

    ClinicalDataCountFilter filter = new ClinicalDataCountFilter();
    filter.setAttributes(List.of(clinicalDataFilter));

    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(TEST_STUDY_ID));
    studyViewFilter.setCustomDataFilters(new java.util.ArrayList<>());
    filter.setStudyViewFilter(studyViewFilter);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/custom-data-counts/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributeId").value(TEST_STABLE_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].value").value("value1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].count").value(3));

    Mockito.verify(studyViewService).getFilteredSamples(Mockito.any(StudyViewFilter.class));

    Mockito.verify(customDataFilterUtil).getCustomDataCounts(Mockito.anyList(), Mockito.anyMap());
  }

  @Test
  public void fetchCustomDataBinCounts() throws Exception {
    ClinicalDataBin bin = new ClinicalDataBin();
    bin.setAttributeId(TEST_STABLE_ID);
    bin.setSpecialValue(null);
    bin.setStart(new BigDecimal("0"));
    bin.setEnd(new BigDecimal("10"));
    bin.setCount(5);

    Mockito.when(
            basicDataBinner.getDataBins(
                Mockito.any(), Mockito.any(ClinicalDataBinCountFilter.class), Mockito.eq(true)))
        .thenReturn(List.of(bin));

    ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
    clinicalDataBinFilter.setAttributeId(TEST_STABLE_ID);

    ClinicalDataBinCountFilter filter = new ClinicalDataBinCountFilter();
    filter.setAttributes(List.of(clinicalDataBinFilter));

    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(List.of(TEST_STUDY_ID));
    filter.setStudyViewFilter(studyViewFilter);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/custom-data-bin-counts/fetch")
                .param("dataBinMethod", "DYNAMIC")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributeId").value(TEST_STABLE_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].start").value(0))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].end").value(10))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(5));

    Mockito.verify(basicDataBinner)
        .getDataBins(
            Mockito.any(), Mockito.any(ClinicalDataBinCountFilter.class), Mockito.eq(true));
  }
}
