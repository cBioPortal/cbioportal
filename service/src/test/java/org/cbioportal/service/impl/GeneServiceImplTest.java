/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.Gene;
import org.cbioportal.service.GeneService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author jiaojiao
 */
@RunWith(MockitoJUnitRunner.class)
@WebAppConfiguration
public class GeneServiceImplTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
     
    private MockMvc mockMvc;
    
    @InjectMocks
    private GeneServiceImpl geneServiceMock;
    
    @Before
    public void setup() {
        Mockito.reset(geneServiceMock);
        
    }

    public GeneServiceImplTest() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    
    @Test
    public void genesByHugoSymbolsDataTest() throws Exception {
        
        List<Gene> mockResponse = new ArrayList<>();
        Gene gene1 = new Gene(); 
        gene1.setEntrezGeneId(673);
        gene1.setHugoGeneSymbol("BRAF");
        gene1.setType("protein-coding");
        gene1.setCytoband("7q34");    
        gene1.setLength(4564);
        Gene gene2 = new Gene();
        gene2.setEntrezGeneId(1956);
        gene2.setHugoGeneSymbol("EGFR");
        gene2.setType("protein-coding");
        gene2.setCytoband("7p12");    
        gene2.setLength(12961);
      	mockResponse.add(gene1);
        mockResponse.add(gene2);
        Mockito.when(geneServiceMock.getGeneListByHugoSymbols(org.mockito.Matchers.anyListOf(String.class))).thenReturn(mockResponse);
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/geneListByHugoSymbols")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .param("hugoSymbols", "BRAF,EGFR"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                //.andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrez_gene_id").value("673"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugo_gene_symbol").value("BRAF"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").value("protein-coding"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value("7q34"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].length").value("4564"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrez_gene_id").value("1956"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugo_gene_symbol").value("EGFR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].type").value("protein-coding"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value("7p12"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].length").value("12961"))
                ;
    }
}
