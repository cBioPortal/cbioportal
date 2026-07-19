package org.cbioportal.application.seo;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.web.config.TestConfig;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {RobotsController.class, SitemapController.class, TestConfig.class})
@TestPropertySource(properties = {"sitemaps=true"})
public class SitemapControllerTest {

  @MockBean private StudyService studyService;
  @MockBean private PatientService patientService;

  @Autowired private MockMvc mockMvc;

  @Test
  public void robotsTxtEmitsPolicyWithSitemap() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/robots.txt"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
        .andExpect(content().string(Matchers.containsString("User-agent: PetalBot")))
        .andExpect(content().string(Matchers.containsString("User-agent: *")))
        .andExpect(content().string(Matchers.containsString("Disallow: /proxy/")))
        .andExpect(content().string(Matchers.containsString("Crawl-delay: 5")))
        .andExpect(
            content()
                .string(Matchers.containsString("Sitemap: http://localhost/sitemap_index.xml")))
        // /api/ must stay crawlable: patient/study pages are a client-rendered SPA that needs it.
        .andExpect(content().string(Matchers.not(Matchers.containsString("Disallow: /api/"))));
  }

  @Test
  public void sitemapIndexPaginatesLargeStudies() throws Exception {
    CancerStudy small = study("small_study", 10);
    // Sample count exceeds one file; the exact patient count decides the page split.
    CancerStudy large = study("large_study", 60000);
    Mockito.when(
            studyService.getAllStudies(
                Mockito.any(),
                Mockito.any(),
                Mockito.anyInt(),
                Mockito.anyInt(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any()))
        .thenReturn(List.of(small, large));
    BaseMeta largeMeta = new BaseMeta();
    largeMeta.setTotalCount(60000);
    Mockito.when(patientService.getMetaPatientsInStudy("large_study")).thenReturn(largeMeta);

    mockMvc
        .perform(MockMvcRequestBuilders.get("/sitemap_index.xml"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
        .andExpect(header().string("X-Robots-Tag", "noindex"))
        // small study: single page; the & separator must be XML-escaped.
        .andExpect(
            content()
                .string(
                    Matchers.containsString("sitemap_study.xml?studyId=small_study&amp;page=0")))
        // large study: 1 + 60000 URLs across two 50000-URL files.
        .andExpect(
            content()
                .string(
                    Matchers.containsString("sitemap_study.xml?studyId=large_study&amp;page=0")))
        .andExpect(
            content()
                .string(
                    Matchers.containsString("sitemap_study.xml?studyId=large_study&amp;page=1")))
        .andExpect(
            content()
                .string(
                    Matchers.not(
                        Matchers.containsString(
                            "sitemap_study.xml?studyId=large_study&amp;page=2"))));
  }

  @Test
  public void sitemapStudyListsStudyAndPatientUrls() throws Exception {
    Mockito.when(
            patientService.getAllPatientsInStudy(
                Mockito.eq("acc_tcga"),
                Mockito.any(),
                Mockito.anyInt(),
                Mockito.anyInt(),
                Mockito.any(),
                Mockito.any()))
        .thenReturn(List.of(patient("TCGA-OR-A5J1"), patient("TCGA-OR-A5J2")));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/sitemap_study.xml").param("studyId", "acc_tcga"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML))
        .andExpect(header().string("X-Robots-Tag", "noindex"))
        .andExpect(
            content().string(Matchers.containsString("http://localhost/study/summary?id=acc_tcga")))
        .andExpect(
            content()
                .string(
                    Matchers.containsString(
                        "http://localhost/patient?studyId=acc_tcga&amp;caseId=TCGA-OR-A5J1")))
        .andExpect(
            content()
                .string(
                    Matchers.containsString(
                        "http://localhost/patient?studyId=acc_tcga&amp;caseId=TCGA-OR-A5J2")));
  }

  @Test
  public void sitemapStudySlicesUrlsPerPage() throws Exception {
    // 50001 patients + 1 study URL = 50002 URLs -> page 0 holds 50000, page 1 holds the last 2.
    List<Patient> patients = new ArrayList<>(50001);
    for (int i = 0; i < 50001; i++) {
      patients.add(patient(String.format("P%06d", i)));
    }
    Mockito.when(
            patientService.getAllPatientsInStudy(
                Mockito.eq("big"),
                Mockito.any(),
                Mockito.anyInt(),
                Mockito.anyInt(),
                Mockito.any(),
                Mockito.any()))
        .thenReturn(patients);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/sitemap_study.xml")
                .param("studyId", "big")
                .param("page", "0"))
        .andExpect(status().isOk())
        .andExpect(content().string(countMatches("<loc>", 50000)));
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/sitemap_study.xml")
                .param("studyId", "big")
                .param("page", "1"))
        .andExpect(status().isOk())
        .andExpect(content().string(countMatches("<loc>", 2)));
    // Page beyond the last is out of range.
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/sitemap_study.xml")
                .param("studyId", "big")
                .param("page", "2"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void sitemapStudyReturnsNotFoundForUnknownStudy() throws Exception {
    Mockito.when(
            patientService.getAllPatientsInStudy(
                Mockito.eq("nope"),
                Mockito.any(),
                Mockito.anyInt(),
                Mockito.anyInt(),
                Mockito.any(),
                Mockito.any()))
        .thenThrow(new StudyNotFoundException("nope"));

    mockMvc
        .perform(MockMvcRequestBuilders.get("/sitemap_study.xml").param("studyId", "nope"))
        .andExpect(status().isNotFound());
  }

  private static CancerStudy study(String id, int sampleCount) {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier(id);
    study.setAllSampleCount(sampleCount);
    return study;
  }

  private static Patient patient(String stableId) {
    Patient patient = new Patient();
    patient.setStableId(stableId);
    return patient;
  }

  private static org.hamcrest.Matcher<String> countMatches(String needle, int expected) {
    return new org.hamcrest.TypeSafeMatcher<>() {
      @Override
      protected boolean matchesSafely(String actual) {
        int count = 0;
        int idx = 0;
        while ((idx = actual.indexOf(needle, idx)) != -1) {
          count++;
          idx += needle.length();
        }
        return count == expected;
      }

      @Override
      public void describeTo(org.hamcrest.Description description) {
        description.appendText(
            "a string containing '" + needle + "' exactly " + expected + " times");
      }
    };
  }
}
