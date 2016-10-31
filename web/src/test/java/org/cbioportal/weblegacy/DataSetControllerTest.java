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
import org.cbioportal.model.DataSet;
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
import org.cbioportal.service.DataSetService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {DataSetControllerConfig.class, CustomObjectMapper.class})
public class DataSetControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private DataSetService dataSetServiceMock;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        Mockito.reset(dataSetServiceMock);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    @Test
    public void getDatasetsTest() throws Exception {
        List<DataSet> mockResponse = new ArrayList<>();
        DataSet dataSet1 = new DataSet();
        dataSet1.setCancer_study_identifier("mbl_icgc_2014");
        dataSet1.setCitation("Kool M et al. Cancer Cell 2014");
        dataSet1.setCount(128);
        dataSet1.setName("Medulloblastoma (ICGC, Cancer Cell 2014)");
        dataSet1.setPMID("24651015");
        dataSet1.setStable_id("mbl_icgc_2014_sequenced");
        DataSet dataSet2 = new DataSet();
        dataSet2.setCancer_study_identifier("mbl_icgc_2014");
        dataSet2.setCitation("Kool M et al. Cancer Cell 2014");
        dataSet2.setCount(128);
        dataSet2.setName("Medulloblastoma (ICGC, Cancer Cell 2014)");
        dataSet2.setPMID("24651015");
        dataSet2.setStable_id("mbl_icgc_2014_all");
        mockResponse.add(dataSet1);
        mockResponse.add(dataSet2);
        Mockito.when(
                dataSetServiceMock.getDataSets())
                .thenReturn(mockResponse);
        this.mockMvc.perform(
                MockMvcRequestBuilders.get("/datasets")
                .accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Medulloblastoma (ICGC, Cancer Cell 2014)"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].citation").value("Kool M et al. Cancer Cell 2014"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancer_study_identifier").value("mbl_icgc_2014"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].stable_id").value("mbl_icgc_2014_sequenced"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(128))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].pmid").value("24651015"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Medulloblastoma (ICGC, Cancer Cell 2014)"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].citation").value("Kool M et al. Cancer Cell 2014"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancer_study_identifier").value("mbl_icgc_2014"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].stable_id").value("mbl_icgc_2014_all"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value(128))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].pmid").value("24651015"));
    }
}
