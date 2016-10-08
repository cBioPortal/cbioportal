/*
 * Copyright (c) 2016 Memorial Sloan Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.cbioportal.weblegacy;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.Gene;
import org.cbioportal.service.GeneService;
import org.cbioportal.web.config.CustomObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {GeneControllerConfig.class, CustomObjectMapper.class})
public class GeneControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private GeneService geneServiceMock;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        Mockito.reset(geneServiceMock);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
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
                MockMvcRequestBuilders.get("/gene/fetch-by-hugo")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .param("hugo_gene_symbols", "BRAF,EGFR"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(673))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value("BRAF"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].type").value("protein-coding"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value("7q34"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].length").value(4564))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(1956))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value("EGFR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].type").value("protein-coding"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value("7p12"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].length").value(12961))
                ;
    }
}
