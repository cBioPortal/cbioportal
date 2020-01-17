/*
 * Copyright (c) 2019 The Hyve B.V.
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
package org.cbioportal.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.cbioportal.model.Treatment;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.TreatmentFilter;
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

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web.xml")
@Configuration
public class TreatmentControllerTest {

    public static final String TREATMENT_ID_1 = "treatment_id_1";
    private static final Integer INTERNAL_ID_1 = 1;
    private static final String DESCRIPTION_1 = "description 1";
    private static final String REF_LINK_1 = "http://link1";
    public static final String TREATMENT_ID_2 = "treatment_id_2";
    private static final Integer INTERNAL_ID_2 = 2;
    private static final String DESCRIPTION_2 = "description 2";
    private static final String REF_LINK_2 = "http://link2";
    public static final String TREATMENT_ID_3 = "treatment_id_3";
    private static final Integer INTERNAL_ID_3 = 3;
    private static final String DESCRIPTION_3 = "description 3";
    private static final String REF_LINK_3 = "http://link3";
    public static final String STUDY_ID_1 = "study_id_1";
    public static final String STUDY_ID_2 = "study_id_2";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private TreatmentService treatmentService;
    private MockMvc mockMvc;

    @Bean
    public TreatmentService treatmentService() {
        return Mockito.mock(TreatmentService.class);
    }

    private int treatmentCount = 2;
    private int studyCount = 3;
    private List<Treatment> treatmentList;
    private List<Treatment> treatmentListForStudy;

    @Before
    public void setUp() throws Exception {

        Mockito.reset(treatmentService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        treatmentList = createTreatmentList();
        treatmentListForStudy = createTreatmentListForStudy();

        BaseMeta baseMetaTreatment = new BaseMeta();
        baseMetaTreatment.setTotalCount(treatmentCount);
        BaseMeta baseMetaStudy = new BaseMeta();
        baseMetaStudy.setTotalCount(studyCount);

        Mockito.when(treatmentService.getAllTreatments(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(treatmentList);
        Mockito.when(treatmentService.getMetaTreatmentsInStudies(Mockito.anyList()))
        .thenReturn(baseMetaStudy);
        Mockito.when(treatmentService.getMetaTreatments(Mockito.anyList()))
        .thenReturn(baseMetaTreatment);
        Mockito.when(treatmentService.getTreatmentsInStudies(Mockito.anyList(), Mockito.anyString()))
        .thenReturn(treatmentListForStudy);
        Mockito.when(treatmentService.getTreatments(Mockito.anyList(), Mockito.anyString()))
        .thenReturn(treatmentList);

    }

    @Test
    public void getAllTreatments() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/treatments")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].treatmentId").value(TREATMENT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value(TREATMENT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value(DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].refLink").value(REF_LINK_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].treatmentId").value(TREATMENT_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value(TREATMENT_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value(DESCRIPTION_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].refLink").value(REF_LINK_2));
    }

    @Test
    public void testFetchMetaTreatmentsByStudyIds() throws Exception {

        TreatmentFilter studyFilter = new TreatmentFilter();
        studyFilter.setStudyIds(Arrays.asList(STUDY_ID_1, STUDY_ID_2));

        mockMvc.perform(MockMvcRequestBuilders
                .post("/treatments/fetch")
                .param("projection", "META")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(studyCount)) );

    }

    @Test
    public void testFetchMetaTreatmentsByTreatmentIds() throws Exception {

        TreatmentFilter treatmentFilter = new TreatmentFilter();
        treatmentFilter.setTreatmentIds(Arrays.asList(TREATMENT_ID_1, TREATMENT_ID_2));

        mockMvc.perform(MockMvcRequestBuilders
                .post("/treatments/fetch")
                .param("projection", "META")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(treatmentFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(treatmentCount)));

    }

    @Test
    public void testFetchMetaTreatmentsNoParam() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                .post("/treatments/fetch")
                .param("projection", "META")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void testFetchTreatmentsByStudyIds() throws Exception {

        TreatmentFilter studyFilter = new TreatmentFilter();
        studyFilter.setStudyIds(Arrays.asList(STUDY_ID_1, STUDY_ID_2));

        mockMvc.perform(MockMvcRequestBuilders
                .post("/treatments/fetch")
                .param("projection", "SUMMARY")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize( treatmentListForStudy.size() )))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].treatmentId").value(TREATMENT_ID_3));
    }

    @Test
    public void testFetchTreatmentsByTreatmentIds() throws Exception {

        TreatmentFilter treatmentFilter = new TreatmentFilter();
        treatmentFilter.setTreatmentIds(Arrays.asList(TREATMENT_ID_1, TREATMENT_ID_2));

        mockMvc.perform(MockMvcRequestBuilders
                .post("/treatments/fetch")
                .param("projection", "SUMMARY")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(treatmentFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize( treatmentList.size() )))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].treatmentId").value(TREATMENT_ID_1));
    }

    @Test
    public void testFetchTreatmentsNoParam() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                .post("/treatments/fetch")
                .param("projection", "SUMMARY")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    public void getTreatment() throws Exception {

        Treatment treatment = createTreatmentList().get(0);
        Mockito.when(treatmentService.getTreatment(Mockito.anyString())).thenReturn(treatment);

        //test /treatments/{treatmentId}
        mockMvc.perform(MockMvcRequestBuilders.get("/treatments/test_treatment_id")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.treatmentId").value(TREATMENT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(TREATMENT_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value(DESCRIPTION_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refLink").value(REF_LINK_1));
    }

    private List<Treatment> createTreatmentList() {

        List<Treatment> treatmentList = new ArrayList<>();

        Treatment treatment1 = new Treatment();
        treatment1.setId(INTERNAL_ID_1);
        treatment1.setStableId(TREATMENT_ID_1);
        treatment1.setName(TREATMENT_ID_1);
        treatment1.setDescription(DESCRIPTION_1);
        treatment1.setRefLink(REF_LINK_1);
        treatmentList.add(treatment1);

        Treatment treatment2 = new Treatment();
        treatment2.setId(INTERNAL_ID_2);
        treatment2.setStableId(TREATMENT_ID_2);
        treatment2.setName(TREATMENT_ID_2);
        treatment2.setDescription(DESCRIPTION_2);
        treatment2.setRefLink(REF_LINK_2);
        treatmentList.add(treatment2);

        return treatmentList;
    }

    private List<Treatment> createTreatmentListForStudy() {

        List<Treatment> treatmentList = new ArrayList<>();

        Treatment treatment3 = new Treatment();
        treatment3.setId(INTERNAL_ID_3);
        treatment3.setStableId(TREATMENT_ID_3);
        treatment3.setName(TREATMENT_ID_3);
        treatment3.setDescription(DESCRIPTION_3);
        treatment3.setRefLink(REF_LINK_3);
        treatmentList.add(treatment3);

        return treatmentList;
    }
}
