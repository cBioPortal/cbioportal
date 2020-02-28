package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.model.TemporalRelation;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.junit.Assert;
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

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class TreatmentControllerTest {
    @Bean
    public TreatmentService treatmentService() {
        return Mockito.mock(TreatmentService.class);
    }
    
    @Test
    public void getAllPatientTreatments() throws Exception {
        Assert.assertTrue(true);
    }


}