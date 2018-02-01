package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.FractionGenomeAltered;
import org.cbioportal.service.FractionGenomeAlteredService;
import org.cbioportal.web.parameter.FractionGenomeAlteredFilter;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web.xml")
@Configuration
public class FractionGenomeAlteredControllerTest {

    private static final String TEST_STUDY_ID = "test_study_id";
    private static final String TEST_SAMPLE_ID_1 = "test_sample_id_1";
    private static final String TEST_SAMPLE_ID_2 = "test_sample_id_2";
    private static final BigDecimal TEST_VALUE_1 = new BigDecimal(2.1);
    private static final BigDecimal TEST_VALUE_2 = new BigDecimal(3.1);
    private static final double CUTOFF = 0.2;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private FractionGenomeAlteredService fractionGenomeAlteredService;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public FractionGenomeAlteredService fractionGenomeAlteredService() {
        return Mockito.mock(FractionGenomeAlteredService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(fractionGenomeAlteredService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
    
    @Test
    public void fetchFractionGenomeAltered() throws Exception {

        List<FractionGenomeAltered> fractionGenomeAlteredList = new ArrayList<>();
        FractionGenomeAltered fractionGenomeAltered1 = new FractionGenomeAltered();
        fractionGenomeAltered1.setStudyId(TEST_STUDY_ID);
        fractionGenomeAltered1.setSampleId(TEST_SAMPLE_ID_1);
        fractionGenomeAltered1.setValue(TEST_VALUE_1);
        fractionGenomeAlteredList.add(fractionGenomeAltered1);
        FractionGenomeAltered fractionGenomeAltered2 = new FractionGenomeAltered();
        fractionGenomeAltered2.setStudyId(TEST_STUDY_ID);
        fractionGenomeAltered2.setSampleId(TEST_SAMPLE_ID_2);
        fractionGenomeAltered2.setValue(TEST_VALUE_2);
        fractionGenomeAlteredList.add(fractionGenomeAltered2);
        
        Mockito.when(fractionGenomeAlteredService.fetchFractionGenomeAltered(TEST_STUDY_ID, 
            Arrays.asList(TEST_SAMPLE_ID_1, TEST_SAMPLE_ID_2), CUTOFF)).thenReturn(fractionGenomeAlteredList);

        FractionGenomeAlteredFilter fractionGenomeAlteredFilter = new FractionGenomeAlteredFilter();
        fractionGenomeAlteredFilter.setSampleIds(Arrays.asList(TEST_SAMPLE_ID_1, TEST_SAMPLE_ID_2));

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/studies/test_study_id/fraction-genome-altered/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(fractionGenomeAlteredFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TEST_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_STUDY_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TEST_VALUE_2));
    }
}
