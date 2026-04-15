package org.cbioportal.application.rest.vcolumnstore;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import org.cbioportal.domain.studyview.StudyViewService;
import org.cbioportal.infrastructure.service.BasicDataBinner;
import org.cbioportal.infrastructure.service.ClinicalDataBinner;
import org.cbioportal.legacy.service.ClinicalDataDensityPlotService;
import org.cbioportal.legacy.service.CustomDataService;
import org.cbioportal.legacy.service.ViolinPlotService;
import org.cbioportal.legacy.web.columnar.util.CustomDataFilterUtil;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.GenomicDataCountFilter;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ColumnarStoreStudyViewController.class, TestConfig.class})
public class ColumnarStoreStudyViewControllerTest {

  @Autowired private MockMvc mockMvc;

  private ObjectMapper objectMapper = new ObjectMapper();

  @MockitoBean private StudyViewService studyViewService;

  @MockitoBean private BasicDataBinner basicDataBinner;

  @MockitoBean private ClinicalDataBinner clinicalDataBinner;

  @MockitoBean private ClinicalDataDensityPlotService clinicalDataDensityPlotService;

  @MockitoBean private ViolinPlotService violinPlotService;

  @MockitoBean private CustomDataService customDataService;

  @MockitoBean private CustomDataFilterUtil customDataFilterUtil;

  private static final String TEST_STUDY_ID = "test_study";

  @Test
  @WithMockUser
  public void fetchGenomicDataCountsWithEmptyFiltersReturnsEmptyList() throws Exception {
    GenomicDataCountFilter filter = new GenomicDataCountFilter();
    filter.setGenomicDataFilters(Collections.emptyList());

    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
    filter.setStudyViewFilter(studyViewFilter);

    mockMvc
        .perform(
            post("/api/column-store/genomic-data-counts/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(studyViewService, never())
        .getCNACountsByGeneSpecific(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyList());
  }

  @Test
  @WithMockUser
  public void fetchGenomicDataCountsWithNullFiltersReturnsEmptyList() throws Exception {
    GenomicDataCountFilter filter = new GenomicDataCountFilter();
    filter.setGenomicDataFilters(null);

    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
    filter.setStudyViewFilter(studyViewFilter);

    mockMvc
        .perform(
            post("/api/column-store/genomic-data-counts/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(studyViewService, never())
        .getCNACountsByGeneSpecific(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyList());
  }

  @Test
  @WithMockUser
  public void fetchMutationDataCountsWithEmptyFiltersReturnsEmptyList() throws Exception {
    GenomicDataCountFilter filter = new GenomicDataCountFilter();
    filter.setGenomicDataFilters(Collections.emptyList());

    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
    filter.setStudyViewFilter(studyViewFilter);

    mockMvc
        .perform(
            post("/api/column-store/mutation-data-counts/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(studyViewService, never())
        .getMutationCountsByGeneSpecific(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyList());
  }

  @Test
  @WithMockUser
  public void fetchMutationDataCountsWithNullFiltersReturnsEmptyList() throws Exception {
    GenomicDataCountFilter filter = new GenomicDataCountFilter();
    filter.setGenomicDataFilters(null);

    StudyViewFilter studyViewFilter = new StudyViewFilter();
    studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
    filter.setStudyViewFilter(studyViewFilter);

    mockMvc
        .perform(
            post("/api/column-store/mutation-data-counts/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

    verify(studyViewService, never())
        .getMutationCountsByGeneSpecific(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyList());
  }
}
