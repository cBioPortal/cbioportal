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

import org.mskcc.cbio.portal.service.MutationalSignatureService;
import org.cbioportal.web.config.CustomObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mskcc.cbio.portal.model.MutationalSignature;
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
@ContextConfiguration(classes = {MutationalSignatureControllerConfig.class, CustomObjectMapper.class, CacheMapUtilConfig.class})
public class MutationalSignatureControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MutationalSignatureService mutationalSignatureServiceMock;
    private MockMvc mockMvc;

    private MutationalSignature signature1;
    private MutationalSignature signature2;

    private static final String[] CANONICAL_SNP_TYPES = new String[]{"C>A", "C>G", "C>T", "T>A", "T>C", "T>G"};

    @Before
    public void setup() {
        Mockito.reset(mutationalSignatureServiceMock);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        signature1 = new MutationalSignature(CANONICAL_SNP_TYPES, "sample1", new int[]{40,0,0,0,0,1});
        signature2 = new MutationalSignature(CANONICAL_SNP_TYPES, "sample2", new int[]{1,2,99,6,4,1});
    }

   @Test
   public void mutationalSignaturesBySampleIdTest() throws Exception {
        List<MutationalSignature> mockResponse = new ArrayList<>();
        mockResponse.add(signature1);
        Mockito.when(mutationalSignatureServiceMock.getMutationalSignaturesBySampleIds(ArgumentMatchers.anyString(), ArgumentMatchers.anyList()))
            .thenReturn(mockResponse);

        String[] sample_ids = new String[]{"sample1"};

        this.mockMvc.perform(
            MockMvcRequestBuilders.get("/mutational-signature")
            .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
            .param("study_id", "msk")
            .param("sample_ids", sample_ids))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json;charset=UTF-8"))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample").value("sample1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0]").value(40))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1]").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[2]").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[3]").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[4]").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[5]").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutationTypes", Matchers.hasSize(6)));
    }

    @Test
    public void mutationalSignaturesByStudyTest() throws Exception {
        List<MutationalSignature> mockResponse = new ArrayList<>();
        mockResponse.add(signature1);
        mockResponse.add(signature2);
        Mockito.when(mutationalSignatureServiceMock.getMutationalSignatures(ArgumentMatchers.anyString()))
            .thenReturn(mockResponse);

        this.mockMvc.perform(
            MockMvcRequestBuilders.get("/mutational-signature")
            .accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
            .param("study_id", "msk"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("application/json;charset=UTF-8"))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample").value("sample1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0]").value(40))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1]").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[2]").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[3]").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[4]").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[5]").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].mutationTypes", Matchers.hasSize(6)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample").value("sample1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0]").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1]").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[2]").value(99))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[3]").value(6))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[4]").value(4))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[5]").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].mutationTypes", Matchers.hasSize(6)));
    }
}
