package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.web.config.TestConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {DocRedirectController.class, TestConfig.class})
@Ignore
// TODO: Had to disable docredirect Controller
public class DocRedirectControllerTest {
    @Autowired
    private WebApplicationContext wac;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void shouldRedirectForDocs() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api-docs"))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.redirectedUrl("/api/v2/api-docs?group=default"));
    }

    @Test
    public void shouldRedirectForUI() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.redirectedUrl("/api/swagger-ui/index.html?group=default"));

        mockMvc.perform(MockMvcRequestBuilders.get("/swagger-ui.html"))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.redirectedUrl("/api/swagger-ui/index.html?group=default"));
    }
}