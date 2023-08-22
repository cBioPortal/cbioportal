package org.cbioportal.web;

import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.SignificantCopyNumberRegionService;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class SignificantCopyNumberRegionControllerTest {

    private static final Long TEST_GISTIC_ROI_ID_1 = 1L;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final int TEST_CHROMOSOME_1 = 1;
    private static final String TEST_CYTOBAND_1 = "1q1.1";
    private static final int TEST_WIDE_PEAK_START_1 = 123;
    private static final int TEST_WIDE_PEAK_END_1 = 136;
    private static final BigDecimal TEST_Q_VALUE_1 = new BigDecimal(0.1);
    private static final Boolean TEST_AMP_1 = false;
    private static final int TEST_ENTREZ_GENE_ID_1 = 1;
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final int TEST_ENTREZ_GENE_ID_2 = 2;
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final Long TEST_GISTIC_ROI_ID_2 = 2L;
    private static final int TEST_CHROMOSOME_2 = 2;
    private static final String TEST_CYTOBAND_2 = "2q1.1";
    private static final int TEST_WIDE_PEAK_START_2 = 223;
    private static final int TEST_WIDE_PEAK_END_2 = 236;
    private static final BigDecimal TEST_Q_VALUE_2 = new BigDecimal(0.2);
    private static final Boolean TEST_AMP_2 = true;
    private static final int TEST_ENTREZ_GENE_ID_3 = 3;
    private static final String TEST_HUGO_GENE_SYMBOL_3 = "test_hugo_gene_symbol_3";
    private static final int TEST_ENTREZ_GENE_ID_4 = 4;
    private static final String TEST_HUGO_GENE_SYMBOL_4 = "test_hugo_gene_symbol_4";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SignificantCopyNumberRegionService significantCopyNumberRegionService;

    private MockMvc mockMvc;

    @Bean
    public SignificantCopyNumberRegionService significantCopyNumberRegionService() {
        return Mockito.mock(SignificantCopyNumberRegionService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(significantCopyNumberRegionService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getSignificantCopyNumberRegions() throws Exception {

        List<Gistic> gisticList = new ArrayList<>();
        Gistic gistic1 = new Gistic();
        gistic1.setGisticRoiId(TEST_GISTIC_ROI_ID_1);
        gistic1.setCancerStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        gistic1.setChromosome(TEST_CHROMOSOME_1);
        gistic1.setCytoband(TEST_CYTOBAND_1);
        gistic1.setWidePeakStart(TEST_WIDE_PEAK_START_1);
        gistic1.setWidePeakEnd(TEST_WIDE_PEAK_END_1);
        gistic1.setqValue(TEST_Q_VALUE_1);
        gistic1.setAmp(TEST_AMP_1);
        List<GisticToGene> gisticToGeneList1 = new ArrayList<>();
        GisticToGene gisticToGene1 = new GisticToGene();
        gisticToGene1.setGisticRoiId(TEST_GISTIC_ROI_ID_1);
        gisticToGene1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        gisticToGene1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        gisticToGeneList1.add(gisticToGene1);
        GisticToGene gisticToGene2 = new GisticToGene();
        gisticToGene2.setGisticRoiId(TEST_GISTIC_ROI_ID_1);
        gisticToGene2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        gisticToGene2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        gisticToGeneList1.add(gisticToGene2);
        gistic1.setGenes(gisticToGeneList1);
        gisticList.add(gistic1);
        Gistic gistic2 = new Gistic();
        gistic2.setGisticRoiId(TEST_GISTIC_ROI_ID_2);
        gistic2.setCancerStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        gistic2.setChromosome(TEST_CHROMOSOME_2);
        gistic2.setCytoband(TEST_CYTOBAND_2);
        gistic2.setWidePeakStart(TEST_WIDE_PEAK_START_2);
        gistic2.setWidePeakEnd(TEST_WIDE_PEAK_END_2);
        gistic2.setqValue(TEST_Q_VALUE_2);
        gistic2.setAmp(TEST_AMP_2);
        List<GisticToGene> gisticToGeneList2 = new ArrayList<>();
        GisticToGene gisticToGene3 = new GisticToGene();
        gisticToGene3.setGisticRoiId(TEST_GISTIC_ROI_ID_2);
        gisticToGene3.setEntrezGeneId(TEST_ENTREZ_GENE_ID_3);
        gisticToGene3.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_3);
        gisticToGeneList2.add(gisticToGene3);
        GisticToGene gisticToGene4 = new GisticToGene();
        gisticToGene4.setGisticRoiId(TEST_GISTIC_ROI_ID_2);
        gisticToGene4.setEntrezGeneId(TEST_ENTREZ_GENE_ID_4);
        gisticToGene4.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_4);
        gisticToGeneList2.add(gisticToGene4);
        gistic2.setGenes(gisticToGeneList2);
        gisticList.add(gistic2);

        Mockito.when(significantCopyNumberRegionService.getSignificantCopyNumberRegions(Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(gisticList);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/significant-copy-number-regions")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].gisticRoiId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].chromosome").value(TEST_CHROMOSOME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].widePeakStart").value(TEST_WIDE_PEAK_START_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].widePeakEnd").value(TEST_WIDE_PEAK_END_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].qValue").value(TEST_Q_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].amp").value(TEST_AMP_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].genes[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].genes[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].genes[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].genes[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].gisticRoiId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].chromosome").value(TEST_CHROMOSOME_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].widePeakStart").value(TEST_WIDE_PEAK_START_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].widePeakEnd").value(TEST_WIDE_PEAK_END_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].qValue").value(TEST_Q_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].amp").value(TEST_AMP_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].genes[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].genes[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].genes[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_4))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].genes[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_4));
    }

    @Test
    public void getSignificantCopyNumberRegionsMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(significantCopyNumberRegionService.getMetaSignificantCopyNumberRegions(Mockito.anyString()))
            .thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/significant-copy-number-regions")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }
}
