package org.cbioportal.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.hamcrest.Matchers;
import org.junit.Ignore;
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
@ContextConfiguration(classes={GeneService.class, GeneController.class, TestConfig.class})
public class GeneControllerTest {

    public static final int ENTREZ_GENE_ID_1 = 1;
    public static final String HUGO_GENE_SYMBOL_1 = "hugo_gene_symbol_1";
    public static final String TYPE_1 = "type_1";
    public static final String CYTOBAND_1 = "cytoband_1";
    public static final String CHROMOSOME_1 = "chromosome_1";
    public static final int ENTREZ_GENE_ID_2 = 2;
    public static final String HUGO_GENE_SYMBOL_2 = "hugo_gene_symbol_2";
    public static final String TYPE_2 = "type_2";
    public static final String CYTOBAND_2 = "cytoband_2";
    public static final String CHROMOSOME_2 = "chromosome_2";
    public static final String ALIAS_1 = "alias_1";
    public static final String ALIAS_2 = "alias_2";

    @MockBean
    private GeneService geneService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getAllGenesDefaultProjection() throws Exception {

        List<Gene> geneList = createGeneList();

        Mockito.when(geneService.getAllGenes(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(geneList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/genes")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(ENTREZ_GENE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(HUGO_GENE_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").value(TYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(ENTREZ_GENE_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(HUGO_GENE_SYMBOL_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].type").value(TYPE_2));

    }

    @Test
    @WithMockUser
    public void getAllGenesMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(ENTREZ_GENE_ID_2);

        Mockito.when(geneService.getMetaGenes(Mockito.any(), Mockito.any())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/genes")
                .param("projection", "META"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    @WithMockUser
    public void getGeneNotFound() throws Exception {

        Mockito.when(geneService.getGene(eq("test_gene_id"))).thenThrow(new GeneNotFoundException("test_gene_id"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/genes/test_gene_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Gene not found: test_gene_id"));
    }

    @Test
    @WithMockUser
    public void getGene() throws Exception {

        List<Gene> geneList = new ArrayList<>();
        Gene gene = new Gene();
        gene.setEntrezGeneId(ENTREZ_GENE_ID_1);
        gene.setHugoGeneSymbol(HUGO_GENE_SYMBOL_1);
        gene.setType(TYPE_1);
        geneList.add(gene);

        Mockito.when(geneService.getGene(Mockito.anyString())).thenReturn(gene);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/genes/test_gene_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.entrezGeneId").value(ENTREZ_GENE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hugoGeneSymbol").value(HUGO_GENE_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.type").value(TYPE_1));
    }

    @Test
    @WithMockUser
    public void getAliasesOfGene() throws Exception {

        List<String> aliasList = new ArrayList<>();
        aliasList.add(ALIAS_1);
        aliasList.add(ALIAS_2);

        Mockito.when(geneService.getAliasesOfGene(Mockito.anyString())).thenReturn(aliasList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/genes/test_gene_id/aliases")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(ALIAS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1]").value(ALIAS_2));
    }

    @Test
    @WithMockUser
    public void fetchGenesDefaultProjection() throws Exception {

        List<Gene> geneList = createGeneList();

        Mockito.when(geneService.fetchGenes(Mockito.anyList(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(geneList);

        List<String> geneIds = new ArrayList<>();
        geneIds.add(Integer.toString(ENTREZ_GENE_ID_1));
        geneIds.add(HUGO_GENE_SYMBOL_2);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/genes/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(geneIds)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(ENTREZ_GENE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(HUGO_GENE_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").value(TYPE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(ENTREZ_GENE_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(HUGO_GENE_SYMBOL_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].type").value(TYPE_2));
    }

    @Test
    @WithMockUser
    public void fetchGenesMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(geneService.fetchMetaGenes(Mockito.anyList(), Mockito.anyString()))
            .thenReturn(baseMeta);

        List<String> geneIds = new ArrayList<>();
        geneIds.add(Integer.toString(ENTREZ_GENE_ID_1));
        geneIds.add(HUGO_GENE_SYMBOL_2);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/genes/fetch").with(csrf())
                .param("projection", "META")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(geneIds)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    private List<Gene> createGeneList() {
        List<Gene> geneList = new ArrayList<>();
        Gene gene1 = new Gene();
        gene1.setEntrezGeneId(ENTREZ_GENE_ID_1);
        gene1.setHugoGeneSymbol(HUGO_GENE_SYMBOL_1);
        gene1.setType(TYPE_1);
        geneList.add(gene1);
        Gene gene2 = new Gene();
        gene2.setEntrezGeneId(ENTREZ_GENE_ID_2);
        gene2.setHugoGeneSymbol(HUGO_GENE_SYMBOL_2);
        gene2.setType(TYPE_2);
        geneList.add(gene2);
        return geneList;
    }
}
