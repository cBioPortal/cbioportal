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
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.Gene;
import org.cbioportal.service.GenePanelService;
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

/**
 *
 * @author heinsz
 */

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {GenePanelControllerConfig.class, CustomObjectMapper.class})
public class GenePanelControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private GenePanelService genePanelServiceMock;
    private MockMvc mockMvc;
    private GenePanel genePanel1;
    private GenePanel genePanel2;

    @Before
    public void setup() {
        Mockito.reset(genePanelServiceMock);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        genePanel1 = new GenePanel();
        genePanel1.setStableId("GENEPANEL2");
        genePanel1.setDescription("2 genes tested");
        genePanel1.setInternalId(1);

        genePanel2 = new GenePanel();
        genePanel2.setStableId("GENEPANEL3");
        genePanel2.setDescription("3 genes tested");
        genePanel2.setInternalId(2);

        List<Gene> genes = new ArrayList<>();
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
        genes.add(gene1);
        genes.add(gene2);

        genePanel1.setGenes(genes);
    }

    @Test
    public void genePanelByStableIdTest() throws Exception {
        List<GenePanel> mockResponse = new ArrayList<>();
        mockResponse.add(genePanel1);
        Mockito.when(genePanelServiceMock.getGenePanelByStableId(org.mockito.Matchers.anyString())).thenReturn(mockResponse);
        this.mockMvc.perform(
        MockMvcRequestBuilders.get("/genepanel")
        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
        .param("panel_id", "GENEPANEL2"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").value("GENEPANEL2"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("2 genes tested"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].genes", Matchers.hasSize(2)));
    }

    @Test
    public void genePanelBySampleIdAndProfileIdTest() throws Exception {
        String mockResponse = "GENEPANEL2";
        Mockito.when(genePanelServiceMock.getGenePanelBySampleIdAndProfileId(org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString())).thenReturn(mockResponse);
        this.mockMvc.perform(
        MockMvcRequestBuilders.get("/genepanel/data")
        .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
        .param("sample_id", "SAMPLE1").param("profile_id", "PROFILE1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
        .andExpect(MockMvcResultMatchers.jsonPath("$").value("GENEPANEL2"));
    }

    @Test
    public void genePanel() throws Exception {
        List<GenePanel> mockResponse = new ArrayList<>();
        mockResponse.add(genePanel1);
        mockResponse.add(genePanel2);
        Mockito.when(genePanelServiceMock.getGenePanels()).thenReturn(mockResponse);
        this.mockMvc.perform(
        MockMvcRequestBuilders.get("/genepanel")
        .accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").value("GENEPANEL2"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("2 genes tested"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].stableId").value("GENEPANEL3"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("3 genes tested"));
    }
}
