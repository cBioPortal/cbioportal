package org.cbioportal.application.seo;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves XML sitemaps that expose every public study and patient page to search engines.
 *
 * <ul>
 *   <li>{@code /sitemap_index.xml} — a sitemap index with one entry per study page (a large study
 *       is split across several pages so no file exceeds the sitemaps.org limits).
 *   <li>{@code /sitemap_study.xml?studyId=X&page=N} — the study-summary URL plus one URL per
 *       patient in that study, for page {@code N}.
 * </ul>
 *
 * <p>Enabled by the {@code sitemaps} program argument (shared with {@link RobotsController}); when
 * disabled every endpoint returns 404. Both files carry {@code X-Robots-Tag: noindex} so the
 * sitemap documents themselves are not indexed.
 */
@Hidden
@RestController
public class SitemapController {

  // sitemaps.org caps a single sitemap at 50,000 URLs / 50 MB. Studies below this stay in one file;
  // larger ones are paginated. The study-summary URL counts toward the per-page total.
  private static final int MAX_URLS_PER_SITEMAP = 50000;

  // getAllStudies / getAllPatientsInStudy paginate; request one oversized page to get everything,
  // matching how StudyController warms its all-studies cache.
  private static final int UNPAGED = 10000000;

  @Value("${sitemaps:false}")
  private boolean sitemapsEnabled;

  @Autowired private StudyService studyService;

  @Autowired private PatientService patientService;

  @GetMapping(value = "/sitemap_index.xml", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> sitemapIndex(HttpServletRequest request) {
    if (!sitemapsEnabled) {
      return ResponseEntity.notFound().build();
    }

    String baseUrl = SeoRequestUtil.resolveBaseUrl(request);

    List<CancerStudy> studies =
        studyService.getAllStudies(
            null,
            Projection.SUMMARY.name(),
            UNPAGED,
            0,
            null,
            Direction.ASC.name(),
            null,
            AccessLevel.READ);

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
    for (CancerStudy study : studies) {
      String studyId = study.getCancerStudyIdentifier();
      int pageCount = pageCountForStudy(study);
      for (int page = 0; page < pageCount; page++) {
        String loc = baseUrl + "/sitemap_study.xml?studyId=" + urlEncode(studyId) + "&page=" + page;
        xml.append("  <sitemap>\n    <loc>")
            .append(xmlEscape(loc))
            .append("</loc>\n  </sitemap>\n");
      }
    }
    xml.append("</sitemapindex>\n");

    return xmlResponse(xml.toString());
  }

  @GetMapping(value = "/sitemap_study.xml", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> sitemapStudy(
      HttpServletRequest request,
      @RequestParam String studyId,
      @RequestParam(defaultValue = "0") int page) {
    if (!sitemapsEnabled) {
      return ResponseEntity.notFound().build();
    }

    String baseUrl = SeoRequestUtil.resolveBaseUrl(request);

    List<Patient> patients;
    try {
      patients =
          patientService.getAllPatientsInStudy(
              studyId, Projection.ID.name(), UNPAGED, 0, null, Direction.ASC.name());
    } catch (StudyNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
    // Sort so a given page returns the same patients on every request; the DB order is not
    // guaranteed. Copy first rather than mutating the list the service handed back.
    patients = new ArrayList<>(patients);
    patients.sort(Comparator.comparing(Patient::getStableId));

    // A study's URL entries are the study-summary URL followed by one URL per patient; paginate
    // that combined sequence into MAX_URLS_PER_SITEMAP-sized pages.
    int totalUrls = 1 + patients.size();
    int pageCount = ceilDiv(totalUrls, MAX_URLS_PER_SITEMAP);
    if (page < 0 || page >= pageCount) {
      return ResponseEntity.notFound().build();
    }

    int start = page * MAX_URLS_PER_SITEMAP;
    int end = Math.min(start + MAX_URLS_PER_SITEMAP, totalUrls);

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
    for (int entry = start; entry < end; entry++) {
      String loc;
      if (entry == 0) {
        loc = baseUrl + "/study/summary?id=" + urlEncode(studyId);
      } else {
        String patientId = patients.get(entry - 1).getStableId();
        loc =
            baseUrl + "/patient?studyId=" + urlEncode(studyId) + "&caseId=" + urlEncode(patientId);
      }
      xml.append("  <url>\n    <loc>").append(xmlEscape(loc)).append("</loc>\n  </url>\n");
    }
    xml.append("</urlset>\n");

    return xmlResponse(xml.toString());
  }

  /**
   * Number of sitemap pages a study needs. Patient count is bounded by sample count, so a study
   * whose samples (plus the study-summary URL) fit in one file is single-page without an extra
   * count query; only genuinely large studies pay for an exact patient count.
   */
  private int pageCountForStudy(CancerStudy study) {
    Integer sampleCount = study.getAllSampleCount();
    if (sampleCount != null && 1 + sampleCount <= MAX_URLS_PER_SITEMAP) {
      return 1;
    }
    int patientCount;
    try {
      patientCount =
          patientService.getMetaPatientsInStudy(study.getCancerStudyIdentifier()).getTotalCount();
    } catch (StudyNotFoundException e) {
      return 1;
    }
    return Math.max(1, ceilDiv(1 + patientCount, MAX_URLS_PER_SITEMAP));
  }

  private ResponseEntity<String> xmlResponse(String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    // The sitemap files list content to index but should not themselves appear in search results.
    headers.add("X-Robots-Tag", "noindex");
    return new ResponseEntity<>(body, headers, HttpStatus.OK);
  }

  private static int ceilDiv(int a, int b) {
    return (a + b - 1) / b;
  }

  private static String urlEncode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  private static String xmlEscape(String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }
}
