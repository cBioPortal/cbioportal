package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.ReferenceGenome;
import org.cbioportal.model.ReferenceGenomeGene;
import org.cbioportal.service.GeneMemoizerService;
import org.cbioportal.service.ReferenceGenomeGeneService;
import org.cbioportal.service.StaticDataTimestampService;
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

import java.util.*;

import static org.mockito.Mockito.times;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class ReferenceGenomeGeneControllerTest {

    public static final String CYTOBAND_1 = "cytoband_1";
    public static final int LENGTH_1 = 100;
    public static final String CHROMOSOME_1 = "chromosome_1";
    public static final String CYTOBAND_2 = "cytoband_2";
    public static final int LENGTH_2 = 200;
    public static final String CHROMOSOME_2 = "chromosome_2";
    public static final int REFERENCE_GENOME_ID = 1;
    public static final int ENTREZ_GENE_ID_1 = 1;
    public static final int ENTREZ_GENE_ID_2 = 2;
    
    private static final ReferenceGenomeGene gene = new ReferenceGenomeGene();
    static {
        gene.setEntrezGeneId(ENTREZ_GENE_ID_1);
        gene.setReferenceGenomeId(REFERENCE_GENOME_ID);
        gene.setCytoband(CYTOBAND_1);
        gene.setChromosome(CHROMOSOME_1);
        gene.setLength(LENGTH_1);
    }

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ReferenceGenomeGeneService referenceGenomeGeneService;

    @Autowired
    private GeneMemoizerService geneMemoizerService;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Bean
    public ReferenceGenomeGeneService referenceGenomeGeneService() {
        return Mockito.mock(ReferenceGenomeGeneService.class);
    }

    @Bean
    public GeneMemoizerService geneMemoizerService() {
        return Mockito.mock(GeneMemoizerService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(referenceGenomeGeneService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
    
    @Test
    public void getAllReferenceGenesFromCache() throws Exception {
        Mockito.when(geneMemoizerService.fetchGenes(Mockito.anyString())).thenReturn(Collections.singletonList(gene));
        Mockito.when(referenceGenomeGeneService.fetchAllReferenceGenomeGenes(Mockito.anyString()))
            .thenReturn(Collections.singletonList(gene));
        
        mockMvc.perform(MockMvcRequestBuilders.get("/reference-genome-genes/hg19")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].referenceGenomeId").value(REFERENCE_GENOME_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].length").value(LENGTH_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].chromosome").value(CHROMOSOME_1));

        
        // The service is not called because the data is already cached
        Mockito.verify(referenceGenomeGeneService, times(0)).fetchAllReferenceGenomeGenes(Mockito.anyString());
        // The cache is not changed
        Mockito.verify(geneMemoizerService, times(0)).cacheGenes(Mockito.anyList(), Mockito.anyString());
    }

    @Test
    public void getAllReferenceGenesNoCache() throws Exception {
        Mockito.when(geneMemoizerService.fetchGenes(Mockito.anyString())).thenReturn(null);
        Mockito.when(referenceGenomeGeneService.fetchAllReferenceGenomeGenes(Mockito.anyString()))
            .thenReturn(Collections.singletonList(gene));
        
        mockMvc.perform(MockMvcRequestBuilders.get("/reference-genome-genes/hg19")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].referenceGenomeId").value(REFERENCE_GENOME_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].length").value(LENGTH_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].chromosome").value(CHROMOSOME_1));

        // The service is called because the cache is invalid
        Mockito.verify(referenceGenomeGeneService, times(1)).fetchAllReferenceGenomeGenes(Mockito.anyString());
        // The response is added to the cache
        Mockito.verify(geneMemoizerService, times(1)).cacheGenes(Mockito.anyList(), Mockito.anyString());
    }

    @Test
    public void getGene() throws Exception {
        Mockito.when(referenceGenomeGeneService.getReferenceGenomeGene(Mockito.anyInt(), Mockito.anyString())).thenReturn(gene);

        mockMvc.perform(MockMvcRequestBuilders.get("/reference-genome-genes/hg19/1")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.entrezGeneId").value(ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.referenceGenomeId").value(REFERENCE_GENOME_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cytoband").value(CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.length").value(LENGTH_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.chromosome").value(CHROMOSOME_1));
    }


    @Test
    public void fetchGenesDefaultProjection() throws Exception {

        List<ReferenceGenomeGene> geneList = createGeneList();

        Mockito.when(referenceGenomeGeneService.fetchGenesByGenomeName(Mockito.anyList(), Mockito.anyString()))
            .thenReturn(geneList);

        List<String> geneIds = new ArrayList<>();
        geneIds.add(Integer.toString(ENTREZ_GENE_ID_1));
        geneIds.add(Integer.toString(ENTREZ_GENE_ID_2));

        mockMvc.perform(MockMvcRequestBuilders.post("/reference-genome-genes/hg19/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(geneIds)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].referenceGenomeId").value(REFERENCE_GENOME_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].length").value(LENGTH_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].chromosome").value(CHROMOSOME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].referenceGenomeId").value(REFERENCE_GENOME_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].length").value(LENGTH_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].chromosome").value(CHROMOSOME_2));
    }


    private List<ReferenceGenomeGene> createGeneList() {
        List<ReferenceGenomeGene> geneList = new ArrayList<>();
        ReferenceGenomeGene gene1 = new ReferenceGenomeGene();
        gene1.setEntrezGeneId(ENTREZ_GENE_ID_1);
        gene1.setCytoband(CYTOBAND_1);
        gene1.setReferenceGenomeId(REFERENCE_GENOME_ID);
        gene1.setChromosome(CHROMOSOME_1);
        gene1.setLength(LENGTH_1);
        geneList.add(gene1);
        ReferenceGenomeGene gene2 = new ReferenceGenomeGene();
        gene2.setEntrezGeneId(ENTREZ_GENE_ID_2);
        gene2.setReferenceGenomeId(REFERENCE_GENOME_ID);
        gene2.setCytoband(CYTOBAND_2);
        gene2.setLength(LENGTH_2);
        gene2.setChromosome(CHROMOSOME_2);
        geneList.add(gene2);
        return geneList;
    }
}

