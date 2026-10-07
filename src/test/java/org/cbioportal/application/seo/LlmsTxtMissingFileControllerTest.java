package org.cbioportal.application.seo;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cbioportal.legacy.web.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/** A configured but unreadable {@code llms_txt.location} returns 404 rather than an error. */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {LlmsTxtController.class, TestConfig.class})
@TestPropertySource(properties = {"llms_txt.location=classpath:seo/does-not-exist.txt"})
public class LlmsTxtMissingFileControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  public void missingFileIsNotFound() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/llms.txt")).andExpect(status().isNotFound());
  }
}
