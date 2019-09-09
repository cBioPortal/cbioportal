package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.cbioportal.service.GenericAssayService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web.xml")
@Configuration
public class GenericAssayControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private GenericAssayService genericAssayService;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public GenericAssayService genericAssayService() {
        return Mockito.mock(GenericAssayService.class);
    }
    
    @Before
    public void setUp() throws Exception {

        Mockito.reset(genericAssayService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
    
    @Test
    public void fetchTreatmentGeneticDataItems() throws Exception {

    }

}