package org.cbioportal.web;

import java.util.HashMap;
import org.cbioportal.service.StaticDataTimestampService;
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
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class StaticDataTimestampControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private StaticDataTimestampService service;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        Mockito.reset(service);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllTimestamps() throws Exception {
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("gene", "1997-08-13 22:00:00");
        Mockito
            .when(service.getTimestamps(Mockito.anyList()))
            .thenReturn(pairs);

        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/timestamps")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(
                MockMvcResultMatchers
                    .jsonPath("$.gene")
                    .value("1997-08-13 22:00:00")
            );
    }
}
