package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.exception.GenePanelNotFoundException;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.GenePanelDataFilter;
import org.cbioportal.web.parameter.GenePanelDataMultipleStudyFilter;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.SampleMolecularIdentifier;
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

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {GenePanelController.class, TestConfig.class})
public class GenePanelControllerTest {

    private static final String TEST_GENE_PANEL_ID_1 = "test_gene_panel_id_1";
    private static final int TEST_INTERNAL_ID_1 = 1;
    private static final String TEST_DESCRIPTION_1 = "test_description_1";
    private static final String TEST_SAMPLE_ID_1 = "test_sample_id_1";
    private static final String TEST_PATIENT_ID_1 = "test_patient_id_1";
    private static final String TEST_STUDY_ID_1 = "test_study_id_1";
    private static final String TEST_MOLECULAR_PROFILE_ID_1 = "test_molecular_profile_id_1";
    private static final int TEST_ENTREZ_GENE_ID_1 = 100;
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final int TEST_ENTREZ_GENE_ID_2 = 200;
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final String TEST_GENE_PANEL_ID_2 = "test_gene_panel_id_2";
    private static final int TEST_INTERNAL_ID_2 = 2;
    private static final String TEST_DESCRIPTION_2 = "test_description_2";
    private static final String TEST_SAMPLE_ID_2 = "test_sample_id_2";
    private static final String TEST_PATIENT_ID_2 = "test_patient_id_2";
    private static final String TEST_STUDY_ID_2 = "test_study_id_2";
    private static final String TEST_MOLECULAR_PROFILE_ID_2 = "test_molecular_profile_id_2";
    private static final int TEST_ENTREZ_GENE_ID_3 = 300;
    private static final String TEST_HUGO_GENE_SYMBOL_3 = "test_hugo_gene_symbol_3";
    private static final int TEST_ENTREZ_GENE_ID_4 = 400;
    private static final String TEST_HUGO_GENE_SYMBOL_4 = "test_hugo_gene_symbol_4";
    private static final String TEST_SAMPLE_LIST_ID = "test_sample_list_id";

    @MockBean
    private GenePanelService genePanelService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getAllGenePanelsDetailedProjection() throws Exception {

        List<GenePanel> genePanelList = new ArrayList<>();
        GenePanel genePanel1 = new GenePanel();
        genePanel1.setStableId(TEST_GENE_PANEL_ID_1);
        genePanel1.setInternalId(TEST_INTERNAL_ID_1);
        genePanel1.setDescription(TEST_DESCRIPTION_1);
        List<GenePanelToGene> genePanelToGeneList1 = new ArrayList<>();
        GenePanelToGene genePanelToGene1 = new GenePanelToGene();
        genePanelToGene1.setGenePanelId(TEST_GENE_PANEL_ID_1);
        genePanelToGene1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        genePanelToGene1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        genePanelToGeneList1.add(genePanelToGene1);
        GenePanelToGene genePanelToGene2 = new GenePanelToGene();
        genePanelToGene2.setGenePanelId(TEST_GENE_PANEL_ID_1);
        genePanelToGene2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        genePanelToGene2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        genePanelToGeneList1.add(genePanelToGene2);
        genePanel1.setGenes(genePanelToGeneList1);
        genePanelList.add(genePanel1);
        GenePanel genePanel2 = new GenePanel();
        genePanel2.setStableId(TEST_GENE_PANEL_ID_2);
        genePanel2.setInternalId(TEST_INTERNAL_ID_2);
        genePanel2.setDescription(TEST_DESCRIPTION_2);
        List<GenePanelToGene> genePanelToGeneList2 = new ArrayList<>();
        GenePanelToGene genePanelToGene3 = new GenePanelToGene();
        genePanelToGene3.setGenePanelId(TEST_GENE_PANEL_ID_2);
        genePanelToGene3.setEntrezGeneId(TEST_ENTREZ_GENE_ID_3);
        genePanelToGene3.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_3);
        genePanelToGeneList2.add(genePanelToGene3);
        GenePanelToGene genePanelToGene4 = new GenePanelToGene();
        genePanelToGene4.setGenePanelId(TEST_GENE_PANEL_ID_2);
        genePanelToGene4.setEntrezGeneId(TEST_ENTREZ_GENE_ID_4);
        genePanelToGene4.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_4);
        genePanelToGeneList2.add(genePanelToGene4);
        genePanel2.setGenes(genePanelToGeneList2);
        genePanelList.add(genePanel2);


        Mockito.when(genePanelService.getAllGenePanels(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(genePanelList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/gene-panels/")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].genePanelId").value(TEST_GENE_PANEL_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].genes[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].genes[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].genes[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].genes[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].stableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].genePanelId").value(TEST_GENE_PANEL_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(TEST_DESCRIPTION_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].genes[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].genes[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].genes[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_4))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].genes[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_4));
    }

    @Test
    @WithMockUser
    public void getAllGenePanelsMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(genePanelService.getMetaGenePanels()).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/gene-panels/")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }
    
    @Test
    @WithMockUser
    public void getGenePanelNotFound() throws Exception {

        Mockito.when(genePanelService.getGenePanel(Mockito.anyString())).thenThrow(
            new GenePanelNotFoundException("test_gene_panel_id"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/gene-panels/test_gene_panel_id")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                .value("Gene panel not found: test_gene_panel_id"));
    }

    @Test
    @WithMockUser
    public void getGenePanel() throws Exception {

        GenePanel genePanel = new GenePanel();
        genePanel.setStableId(TEST_GENE_PANEL_ID_1);
        genePanel.setInternalId(TEST_INTERNAL_ID_1);
        genePanel.setDescription(TEST_DESCRIPTION_1);
        List<GenePanelToGene> genePanelToGeneList = new ArrayList<>();
        GenePanelToGene genePanelToGene1 = new GenePanelToGene();
        genePanelToGene1.setGenePanelId(TEST_GENE_PANEL_ID_1);
        genePanelToGene1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        genePanelToGene1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        genePanelToGeneList.add(genePanelToGene1);
        GenePanelToGene genePanelToGene2 = new GenePanelToGene();
        genePanelToGene2.setGenePanelId(TEST_GENE_PANEL_ID_1);
        genePanelToGene2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        genePanelToGene2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        genePanelToGeneList.add(genePanelToGene2);
        genePanel.setGenes(genePanelToGeneList);

        Mockito.when(genePanelService.getGenePanel(Mockito.anyString())).thenReturn(genePanel);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/gene-panels/test_gene_panel_id")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.stableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.genePanelId").value(TEST_GENE_PANEL_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(TEST_DESCRIPTION_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.genes[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.genes[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.genes[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.genes[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2));
    }
}
