package org.cbioportal.application.seo;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.sort.PatientSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves XML sitemaps that expose every study and patient page to search engines.
 *
 * <ul>
 *   <li>{@code /sitemap_index.xml} — a sitemap index with one entry per study page (a large study
 *       is split across several pages so no file exceeds the sitemaps.org limits).
 *   <li>{@code /sitemap_study.xml?studyId=X&page=N} — the study-summary URL plus one URL per
 *       patient in that study, for page {@code N}.
 * </ul>
 *
 * <p>Available only when the {@link SitemapFeature} is enabled (the {@code sitemaps} flag);
 * otherwise every endpoint returns 404. The index is built from an anonymous study listing so it
 * advertises only public studies, and per-study patient enumeration is guarded by a study-level
 * authorization check, so the feature never exposes non-public data. Both files carry {@code
 * X-Robots-Tag: noindex} so the sitemap documents themselves are not indexed.
 */
@Hidden
@RestController
public class SitemapController {

  // sitemaps.org caps a single sitemap at 50,000 URLs / 50 MB.
  private static final int MAX_URLS_PER_SITEMAP = 50000;

  // Patients are paginated one fewer than the cap so page 0 still fits after the study-summary URL
  // is prepended. Pages line up with the patient-service page boundaries (pageSize = this value).
  private static final int PATIENTS_PER_PAGE = MAX_URLS_PER_SITEMAP - 1;

  // getAllStudies paginates; request one oversized page to get every study, matching how
  // StudyController warms its all-studies cache.
  private static final int UNPAGED = 10000000;

  @Autowired private SitemapFeature sitemapFeature;

  @Autowired private StudyService studyService;

  @Autowired private PatientService patientService;

  @GetMapping(value = "/sitemap_index.xml", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> sitemapIndex(HttpServletRequest request) {
    if (!sitemapFeature.isEnabled()) {
      return ResponseEntity.notFound().build();
    }

    String baseUrl = SeoRequestUtil.resolveBaseUrl(request);

    // Build the index from the anonymous listing (null authentication) so it advertises only public
    // studies, whatever the portal's auth mode.
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

  // A study's patient list is public only for public studies; the permission check limits an
  // anonymous crawler to those and blocks enumeration of access-controlled studies.
  @PreAuthorize(
      "hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @GetMapping(value = "/sitemap_study.xml", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> sitemapStudy(
      HttpServletRequest request,
      @RequestParam String studyId,
      @RequestParam(defaultValue = "0") int page) {
    if (!sitemapFeature.isEnabled()) {
      return ResponseEntity.notFound().build();
    }
    if (page < 0) {
      return ResponseEntity.notFound().build();
    }

    String baseUrl = SeoRequestUtil.resolveBaseUrl(request);

    // Fetch only the patients for the requested page, sorted by stable id so page boundaries are
    // stable across requests. The database does the paging and sorting; no full-study scan.
    List<Patient> patients;
    try {
      patients =
          patientService.getAllPatientsInStudy(
              studyId,
              Projection.ID.name(),
              PATIENTS_PER_PAGE,
              page,
              PatientSortBy.patientId.getOriginalValue(),
              Direction.ASC.name());
    } catch (StudyNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
    // The study-summary URL lives on page 0. A later page with no patients is past the end.
    if (page > 0 && patients.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
    if (page == 0) {
      appendUrl(xml, baseUrl + "/study/summary?id=" + urlEncode(studyId));
    }
    for (Patient patient : patients) {
      appendUrl(
          xml,
          baseUrl
              + "/patient?studyId="
              + urlEncode(studyId)
              + "&caseId="
              + urlEncode(patient.getStableId()));
    }
    xml.append("</urlset>\n");

    return xmlResponse(xml.toString());
  }

  /**
   * Number of sitemap pages a study needs. Patient count is bounded by sample count, so a study
   * whose samples fit on one page is single-page without an extra count query; only genuinely large
   * studies pay for an exact patient count.
   */
  private int pageCountForStudy(CancerStudy study) {
    Integer sampleCount = study.getAllSampleCount();
    if (sampleCount != null && sampleCount <= PATIENTS_PER_PAGE) {
      return 1;
    }
    int patientCount;
    try {
      patientCount =
          patientService.getMetaPatientsInStudy(study.getCancerStudyIdentifier()).getTotalCount();
    } catch (StudyNotFoundException e) {
      return 1;
    }
    return Math.max(1, ceilDiv(patientCount, PATIENTS_PER_PAGE));
  }

  private static void appendUrl(StringBuilder xml, String loc) {
    xml.append("  <url>\n    <loc>").append(xmlEscape(loc)).append("</loc>\n  </url>\n");
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
