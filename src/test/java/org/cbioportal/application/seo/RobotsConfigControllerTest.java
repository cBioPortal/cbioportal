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
@TestPropertySource(properties = {"sitemaps=true", "robots.disallow_user_agents=PetalBot, BadBot"})
public class RobotsConfigControllerTest {

  @MockBean private StudyService studyService;
  @MockBean private PatientService patientService;

  @Autowired private MockMvc mockMvc;

  @Test
  public void blockedUserAgentsGetTheirOwnGroupsBeforeTheWildcard() throws Exception {
    String body =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/robots.txt"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertTrue(body.contains("User-agent: PetalBot\nDisallow: /"));
    // The surrounding whitespace in the property value is trimmed.
    assertTrue(body.contains("User-agent: BadBot\nDisallow: /"));
    // Blocked bots come before the shared wildcard policy, which is still present.
    assertTrue(body.indexOf("User-agent: PetalBot") < body.indexOf("User-agent: *"));
    assertTrue(body.contains("Disallow: /proxy/"));
  }
}
