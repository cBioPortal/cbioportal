/*
 * Copyright (c) 2016 - 2018 Memorial Sloan Kettering Cancer Center.
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

import org.cbioportal.web.config.CacheMapUtilConfig;
import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.model.CNSegmentData;
import org.cbioportal.web.config.CustomObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
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
import org.mskcc.cbio.portal.service.CNSegmentService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {CNSegmentControllerConfig.class, CustomObjectMapper.class, CacheMapUtilConfig.class})
public class CNSegmentControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private CNSegmentService cnSegmentServiceMock;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        Mockito.reset(cnSegmentServiceMock);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void getCNSegmentTest() throws Exception {
        List<CNSegmentData> mockResponse = new ArrayList<>();
        CNSegmentData cnSegmentData1 = new CNSegmentData();
        cnSegmentData1.setStart(148350479);
        cnSegmentData1.setEnd(158385118);
        cnSegmentData1.setChr("7");
        cnSegmentData1.setNumProbes(4901);
        cnSegmentData1.setValue((float) 0.1315);
        cnSegmentData1.setSample("TCGA-AG-3732-01");
        CNSegmentData cnSegmentData2 = new CNSegmentData();
        cnSegmentData2.setStart(148347132);
        cnSegmentData2.setEnd(148348416);
        cnSegmentData2.setChr("7");
        cnSegmentData2.setNumProbes(3);
        cnSegmentData2.setValue((float) -1.5009);
        cnSegmentData2.setSample("TCGA-AG-3732-01");
        mockResponse.add(cnSegmentData1);
        mockResponse.add(cnSegmentData2);
        Mockito.when(
                cnSegmentServiceMock.getCNSegmentData(ArgumentMatchers.anyString(),
                                                        ArgumentMatchers.anyList(),
                                                        ArgumentMatchers.anyList()))
                .thenReturn(mockResponse)
                ;
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/copynumbersegments")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
                .param("cancerStudyId", "coadread_tcga")
                .param("chromosomes", "1,2")
                .param("sampleIds", "TCGA-AG-3732-01"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample").value("TCGA-AG-3732-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].chr").value("7"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].start").value(148350479))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].end").value(158385118))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].numProbes").value(4901))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(0.1315))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sample").value("TCGA-AG-3732-01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].chr").value("7"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].start").value(148347132))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].end").value(148348416))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].numProbes").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(-1.5009))
                ;
    }
}
