package org.cbioportal.application.seo;

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
 * Sitemaps are a public-portal-only feature: even with {@code sitemaps=true}, enabling
 * authorization turns every SEO endpoint into a 404, so an authenticated portal never publishes
 * study/patient URLs.
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
@TestPropertySource(properties = {"sitemaps=true", "authenticate=true"})
public class SitemapAuthEnabledControllerTest {

  @MockBean private StudyService studyService;
  @MockBean private PatientService patientService;

  @Autowired private MockMvc mockMvc;

  @Test
  public void robotsTxtIsNotFoundWhenAuthorizationEnabled() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/robots.txt")).andExpect(status().isNotFound());
  }

  @Test
  public void sitemapIndexIsNotFoundWhenAuthorizationEnabled() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/sitemap_index.xml"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void sitemapStudyIsNotFoundWhenAuthorizationEnabled() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/sitemap_study.xml").param("studyId", "acc_tcga"))
        .andExpect(status().isNotFound());
  }
}
