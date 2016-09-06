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
package org.cbioportal.web.api;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.CNASegmentData;
import org.cbioportal.service.CNASegmentService;
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
public class CNASegmentControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private CNASegmentService cNASegmentServiceMock;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        Mockito.reset(cNASegmentServiceMock);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    @Test
    public void getCNASegmentTest() throws Exception {
        List<CNASegmentData> mockResponse = new ArrayList<>();
        CNASegmentData cNASegmentData1 = new CNASegmentData(); 
        cNASegmentData1.setStart(148350479);
        cNASegmentData1.setEnd(158385118);
        cNASegmentData1.setChromosome("7");
        cNASegmentData1.setNumProbes(4901);
        cNASegmentData1.setSegmentMean((float) 0.1315);
        cNASegmentData1.setSampleStableId("TCGA-AG-3732-01");
        
        CNASegmentData cNASegmentData2 = new CNASegmentData(); 
        cNASegmentData2.setStart(148347132);
        cNASegmentData2.setEnd(148348416);
        cNASegmentData2.setChromosome("7");
        cNASegmentData2.setNumProbes(3);
        cNASegmentData2.setSegmentMean((float) -1.5009);
        cNASegmentData2.setSampleStableId("TCGA-AG-3732-01");
       
        
      	mockResponse.add(cNASegmentData1);
        mockResponse.add(cNASegmentData2);
        Mockito.when(cNASegmentServiceMock.getCNASegmentData(org.mockito.Matchers.anyString(), org.mockito.Matchers.anyListOf(String.class), org.mockito.Matchers.anyListOf(String.class))).thenReturn(mockResponse);
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/cnaSegmentService")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .param("cancerStudyId", "coadread_tcga")
                .param("hugoSymbols", "BRAF,EGFR")
                .param("sampleIds", "TCGA-AG-3732-01"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample_stable_id").value("TCGA-AG-3732-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].chromosome").value("7"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].start").value("148350479"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].end").value("158385118"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].num_probes").value("4901"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].segment_mean").value("0.1315"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sample_stable_id").value("TCGA-AG-3732-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].chromosome").value("7"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].start").value("148347132"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].end").value("148348416"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].num_probes").value("3"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].segment_mean").value("-1.5009"))
                ;
    }
}

