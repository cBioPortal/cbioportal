package org.cbioportal.application.seo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.web.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * With {@code llms_txt.location} set, {@code /llms.txt} serves that file as markdown and robots.txt
 * points to it.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(
    classes = {
      LlmsTxtController.class,
      RobotsController.class,
      SitemapController.class,
      SitemapFeature.class,
      TestConfig.class
    })
@TestPropertySource(properties = {"sitemaps=true", "llms_txt.location=classpath:seo/llms-test.txt"})
public class LlmsTxtControllerTest {

  @MockBean private StudyService studyService;
  @MockBean private PatientService patientService;

  @Autowired private MockMvc mockMvc;

  @Test
  public void servesConfiguredFileAsMarkdown() throws Exception {
    String body =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/llms.txt"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/markdown"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertEquals("# Test portal\n\n> Download data in bulk from https://example.org/dump.\n", body);
  }

  @Test
  public void robotsTxtPointsToLlmsTxt() throws Exception {
    String body =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/robots.txt"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertTrue(body.startsWith("# Guidance for AI agents and bulk data access: "));
    assertTrue(body.contains("/llms.txt\n"));
  }
}
