package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.exception.GeneNotFoundException;
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

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
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

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private GeneService geneService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Bean
    public GeneService geneService() {
        return Mockito.mock(GeneService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(geneService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllGenesDefaultProjection() throws Exception {

        List<Gene> geneList = createGeneList();

        Mockito.when(geneService.getAllGenes(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(geneList);

        mockMvc.perform(MockMvcRequestBuilders.get("/genes")
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
    public void getAllGenesMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(ENTREZ_GENE_ID_2);

        Mockito.when(geneService.getMetaGenes(Mockito.any(), Mockito.any())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/genes")
                .param("projection", "META"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void getGeneNotFound() throws Exception {

        Mockito.when(geneService.getGene(Mockito.anyString())).thenThrow(new GeneNotFoundException("test_gene_id"));

        mockMvc.perform(MockMvcRequestBuilders.get("/genes/test_gene_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Gene not found: test_gene_id"));
    }

    @Test
    public void getGene() throws Exception {

        List<Gene> geneList = new ArrayList<>();
        Gene gene = new Gene();
        gene.setEntrezGeneId(ENTREZ_GENE_ID_1);
        gene.setHugoGeneSymbol(HUGO_GENE_SYMBOL_1);
        gene.setType(TYPE_1);
        geneList.add(gene);

        Mockito.when(geneService.getGene(Mockito.anyString())).thenReturn(gene);

        mockMvc.perform(MockMvcRequestBuilders.get("/genes/test_gene_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.entrezGeneId").value(ENTREZ_GENE_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.hugoGeneSymbol").value(HUGO_GENE_SYMBOL_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.type").value(TYPE_1));
    }

    @Test
    public void getAliasesOfGene() throws Exception {

        List<String> aliasList = new ArrayList<>();
        aliasList.add(ALIAS_1);
        aliasList.add(ALIAS_2);

        Mockito.when(geneService.getAliasesOfGene(Mockito.anyString())).thenReturn(aliasList);

        mockMvc.perform(MockMvcRequestBuilders.get("/genes/test_gene_id/aliases")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(ALIAS_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1]").value(ALIAS_2));
    }

    @Test
    public void fetchGenesDefaultProjection() throws Exception {

        List<Gene> geneList = createGeneList();

        Mockito.when(geneService.fetchGenes(Mockito.anyList(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(geneList);

        List<String> geneIds = new ArrayList<>();
        geneIds.add(Integer.toString(ENTREZ_GENE_ID_1));
        geneIds.add(HUGO_GENE_SYMBOL_2);

        mockMvc.perform(MockMvcRequestBuilders.post("/genes/fetch")
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
    public void fetchGenesMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(geneService.fetchMetaGenes(Mockito.anyList(), Mockito.anyString()))
            .thenReturn(baseMeta);

        List<String> geneIds = new ArrayList<>();
        geneIds.add(Integer.toString(ENTREZ_GENE_ID_1));
        geneIds.add(HUGO_GENE_SYMBOL_2);

        mockMvc.perform(MockMvcRequestBuilders.post("/genes/fetch")
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
