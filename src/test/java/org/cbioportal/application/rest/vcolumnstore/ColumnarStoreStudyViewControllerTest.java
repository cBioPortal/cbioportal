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
import org.cbioportal.legacy.web.parameter.GenomicDataCountFilter;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for ColumnarStoreStudyViewController.
 *
 * When genomicDataFilters is empty or null, the endpoints should return
 * an empty list instead of throwing an IndexOutOfBoundsException.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ColumnarStoreStudyViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudyViewService studyViewService;

    private static final String TEST_STUDY_ID = "test_study";

    @Test
    @WithMockUser
    public void fetchGenomicDataCountsWithEmptyFiltersReturnsEmptyList() throws Exception {
        GenomicDataCountFilter filter = new GenomicDataCountFilter();
        filter.setGenomicDataFilters(Collections.emptyList());

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
        filter.setStudyViewFilter(studyViewFilter);

        mockMvc.perform(post("/api/column-store/genomic-data-counts/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

        // Verify that the service was never called (early return due to empty filters)
        verify(studyViewService, never()).getCNACountsByGeneSpecific(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.anyList()
        );
    }

    @Test
    @WithMockUser
    public void fetchGenomicDataCountsWithNullFiltersReturnsEmptyList() throws Exception {
        GenomicDataCountFilter filter = new GenomicDataCountFilter();
        filter.setGenomicDataFilters(null);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
        filter.setStudyViewFilter(studyViewFilter);

        mockMvc.perform(post("/api/column-store/genomic-data-counts/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

        // Verify that the service was never called (early return due to null filters)
        verify(studyViewService, never()).getCNACountsByGeneSpecific(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.anyList()
        );
    }

    @Test
    @WithMockUser
    public void fetchMutationDataCountsWithEmptyFiltersReturnsEmptyList() throws Exception {
        GenomicDataCountFilter filter = new GenomicDataCountFilter();
        filter.setGenomicDataFilters(Collections.emptyList());

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
        filter.setStudyViewFilter(studyViewFilter);

        mockMvc.perform(post("/api/column-store/mutation-data-counts/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

        // Verify that the service was never called (early return due to empty filters)
        verify(studyViewService, never()).getMutationCountsByGeneSpecific(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.anyList()
        );
    }

    @Test
    @WithMockUser
    public void fetchMutationDataCountsWithNullFiltersReturnsEmptyList() throws Exception {
        GenomicDataCountFilter filter = new GenomicDataCountFilter();
        filter.setGenomicDataFilters(null);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
        filter.setStudyViewFilter(studyViewFilter);

        mockMvc.perform(post("/api/column-store/mutation-data-counts/fetch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

        // Verify that the service was never called (early return due to null filters)
        verify(studyViewService, never()).getMutationCountsByGeneSpecific(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.anyList()
        );
    }
}
