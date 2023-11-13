package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.CopyNumberSegmentService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {CopyNumberSegmentController.class, TestConfig.class})
public class CopyNumberSegmentControllerTest {

    private static final int TEST_SAMPLE_ID_1 = 1;
    private static final String TEST_SAMPLE_STABLE_ID_1 = "test_sample_stable_id_1";
    private static final int TEST_CANCER_STUDY_ID_1 = 1;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final Long TEST_SEG_ID_1 = 1L;
    private static final String TEST_CHR_1 = "test_chr_1";
    private static final Integer TEST_START_1 = 15;
    private static final Integer TEST_END_1 = 20;
    private static final Integer TEST_NUM_PROBES_1 = 3;
    private static final BigDecimal TEST_SEGMENT_MEAN_1 = new BigDecimal(0.2);
    private static final int TEST_SAMPLE_ID_2 = 2;
    private static final String TEST_SAMPLE_STABLE_ID_2 = "test_sample_stable_id_2";
    private static final int TEST_CANCER_STUDY_ID_2 = 2;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_2 = "test_study_2";
    private static final Long TEST_SEG_ID_2 = 2L;
    private static final String TEST_CHR_2 = "test_chr_2";
    private static final Integer TEST_START_2 = 25;
    private static final Integer TEST_END_2 = 34;
    private static final Integer TEST_NUM_PROBES_2 = 5;
    private static final BigDecimal TEST_SEGMENT_MEAN_2 = new BigDecimal(0.4);

    private WebApplicationContext wac;

    @MockBean
    private CopyNumberSegmentService copyNumberSegmentService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getCopyNumberSegmentsInSampleInStudyDefaultProjection() throws Exception {

        List<CopyNumberSeg> copyNumberSegList = createExampleCopyNumberSegs();

        Mockito.when(copyNumberSegmentService.getCopyNumberSegmentsInSampleInStudy(Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(copyNumberSegList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/samples/test_sample_id/copy-number-segments")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].segId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].chromosome").value(TEST_CHR_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].start").value(TEST_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].end").value(TEST_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfProbes").value(TEST_NUM_PROBES_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].segmentMean").value(TEST_SEGMENT_MEAN_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].segId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].chromosome").value(TEST_CHR_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].start").value(TEST_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].end").value(TEST_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfProbes").value(TEST_NUM_PROBES_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].segmentMean").value(TEST_SEGMENT_MEAN_2));
    }

    @Test
    @WithMockUser
    public void getCopyNumberSegmentsInSampleInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(copyNumberSegmentService.getMetaCopyNumberSegmentsInSampleInStudy(Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/samples/test_sample_id/copy-number-segments")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    @WithMockUser
    public void fetchCopyNumberSegmentsDefaultProjection() throws Exception {

        List<CopyNumberSeg> copyNumberSegList = createExampleCopyNumberSegs();

        Mockito.when(copyNumberSegmentService.fetchCopyNumberSegments(Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(copyNumberSegList);

        List<SampleIdentifier> sampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
        sampleIdentifier1.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleIdentifier1.setSampleId(TEST_SAMPLE_STABLE_ID_1);
        sampleIdentifiers.add(sampleIdentifier1);
        SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
        sampleIdentifier2.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleIdentifier2.setSampleId(TEST_SAMPLE_STABLE_ID_2);
        sampleIdentifiers.add(sampleIdentifier2);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/copy-number-segments/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleIdentifiers)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].segId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].chromosome").value(TEST_CHR_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].start").value(TEST_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].end").value(TEST_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfProbes").value(TEST_NUM_PROBES_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].segmentMean").value(TEST_SEGMENT_MEAN_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].segId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].chromosome").value(TEST_CHR_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].start").value(TEST_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].end").value(TEST_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfProbes").value(TEST_NUM_PROBES_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].segmentMean").value(TEST_SEGMENT_MEAN_2));
    }

    @Test
    @WithMockUser
    public void fetchCopyNumberSegmentsMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(copyNumberSegmentService.fetchMetaCopyNumberSegments(Mockito.any(),
            Mockito.any(), Mockito.any())).thenReturn(baseMeta);

        List<SampleIdentifier> sampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
        sampleIdentifier1.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleIdentifier1.setSampleId(TEST_SAMPLE_STABLE_ID_1);
        sampleIdentifiers.add(sampleIdentifier1);
        SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
        sampleIdentifier2.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleIdentifier2.setSampleId(TEST_SAMPLE_STABLE_ID_2);
        sampleIdentifiers.add(sampleIdentifier2);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/copy-number-segments/fetch").with(csrf())
            .param("projection", "META")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleIdentifiers)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    private List<CopyNumberSeg> createExampleCopyNumberSegs() {
        List<CopyNumberSeg> copyNumberSegList = new ArrayList<>();
        CopyNumberSeg copyNumberSeg1 = new CopyNumberSeg();
        copyNumberSeg1.setSegId(TEST_SEG_ID_1);
        copyNumberSeg1.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        copyNumberSeg1.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        copyNumberSeg1.setSampleId(TEST_SAMPLE_ID_1);
        copyNumberSeg1.setSampleStableId(TEST_SAMPLE_STABLE_ID_1);
        copyNumberSeg1.setChr(TEST_CHR_1);
        copyNumberSeg1.setStart(TEST_START_1);
        copyNumberSeg1.setEnd(TEST_END_1);
        copyNumberSeg1.setNumProbes(TEST_NUM_PROBES_1);
        copyNumberSeg1.setSegmentMean(TEST_SEGMENT_MEAN_1);
        copyNumberSegList.add(copyNumberSeg1);
        CopyNumberSeg copyNumberSeg2 = new CopyNumberSeg();
        copyNumberSeg2.setSegId(TEST_SEG_ID_2);
        copyNumberSeg2.setCancerStudyId(TEST_CANCER_STUDY_ID_2);
        copyNumberSeg2.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_2);
        copyNumberSeg2.setSampleId(TEST_SAMPLE_ID_2);
        copyNumberSeg2.setSampleStableId(TEST_SAMPLE_STABLE_ID_2);
        copyNumberSeg2.setChr(TEST_CHR_2);
        copyNumberSeg2.setStart(TEST_START_2);
        copyNumberSeg2.setEnd(TEST_END_2);
        copyNumberSeg2.setNumProbes(TEST_NUM_PROBES_2);
        copyNumberSeg2.setSegmentMean(TEST_SEGMENT_MEAN_2);
        copyNumberSegList.add(copyNumberSeg2);
        return copyNumberSegList;
    }
}
