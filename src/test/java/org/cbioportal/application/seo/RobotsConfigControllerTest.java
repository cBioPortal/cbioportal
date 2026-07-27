package org.cbioportal.application.seo;

import static org.junit.Assert.assertTrue;
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
 * {@code robots.disallow_user_agents} adds a deployment-specific block for each named crawler ahead
 * of the shared policy.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(
    classes = {
      RobotsController.class,
      SitemapController.class,
      SitemapFeature.class,
      TestConfig.class
    })
@TestPropertySource(
    properties = {
      "sitemaps=true",
      "robots.disallow_user_agents=PetalBot, BadBot",
      "robots.disallow_paths=/proxy/, /annotation/",
      "robots.crawl_delay=10"
    })
public class RobotsConfigControllerTest {

  @MockBean private StudyService studyService;
  @MockBean private PatientService patientService;

  @Autowired private MockMvc mockMvc;

  @Test
  public void policyReflectsConfiguredBotsPathsAndCrawlDelay() throws Exception {
    String body =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/robots.txt"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    // Each configured bot gets its own block, before the shared wildcard policy.
    assertTrue(body.contains("User-agent: PetalBot\nDisallow: /"));
    assertTrue(body.contains("User-agent: BadBot\nDisallow: /"));
    assertTrue(body.indexOf("User-agent: PetalBot") < body.indexOf("User-agent: *"));

    // Each configured path becomes a Disallow line (surrounding whitespace trimmed).
    assertTrue(body.contains("Disallow: /proxy/"));
    assertTrue(body.contains("Disallow: /annotation/"));

    // Configured crawl-delay is used.
    assertTrue(body.contains("Crawl-delay: 10"));
  }
}
