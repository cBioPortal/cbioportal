package org.cbioportal.application.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.cbioportal.domain.wsi.WsiHierarchy;
import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.cbioportal.domain.wsi.repository.WsiHierarchyRepository;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** Serves the materialized WSI hierarchy from the cBioPortal ClickHouse store. */
@RestController
@RequestMapping("/api/wsi/v2/hierarchy")
public class WsiHierarchyController {

  private final WsiHierarchyRepository repository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired(required = false)
  private CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator;

  @Value("${wsi.local-auth-bypass:false}")
  private boolean localAuthBypass;

  @Value("${wsi.local-bridge.enabled:false}")
  private boolean localBridgeEnabled;

  @Value("${wsi.local-bridge.url:http://localhost:8081/internal/patient}")
  private String localBridgeUrl;

  public WsiHierarchyController(WsiHierarchyRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/{studyId}/{patientId}")
  public ResponseEntity<WsiHierarchy> getPatientHierarchy(
      @PathVariable String studyId, @PathVariable String patientId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean anonymous =
        authentication == null
            || !authentication.isAuthenticated()
            || authentication instanceof AnonymousAuthenticationToken;

    if (anonymous && !localAuthBypass) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
          .header(HttpHeaders.VARY, "Authorization, Cookie")
          .build();
    }

    if (!anonymous
        && cancerStudyPermissionEvaluator != null
        && !cancerStudyPermissionEvaluator.hasPermission(
            authentication, studyId, "CancerStudyId", AccessLevel.READ)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
          .header(HttpHeaders.VARY, "Authorization, Cookie")
          .build();
    }

    WsiHierarchy hierarchy = null;
    try {
      hierarchy = repository.getPatientHierarchy(studyId, patientId);
    } catch (RuntimeException exception) {
      if (!localBridgeEnabled) {
        throw exception;
      }
    }

    if (hierarchy == null && localBridgeEnabled) {
      hierarchy = fetchLocalBridgeHierarchy(studyId, patientId);
    }

    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      if (hierarchy == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
            .header(HttpHeaders.VARY, "Authorization, Cookie")
            .build();
      }
    }
    if (hierarchy == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
          .header(HttpHeaders.VARY, "Authorization, Cookie")
          .build();
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
        .header(HttpHeaders.VARY, "Authorization, Cookie")
        .contentType(MediaType.APPLICATION_JSON)
        .body(hierarchy);
  }

  private WsiHierarchy fetchLocalBridgeHierarchy(String studyId, String patientId) {
    try {
      URI uri =
          UriComponentsBuilder.fromUriString(localBridgeUrl)
              .pathSegment(patientId)
              .queryParam("studyId", studyId)
              .build(true)
              .toUri();
      String payload = new RestTemplate().getForObject(uri, String.class);
      return payload == null ? null : objectMapper.readValue(payload, WsiHierarchy.class);
    } catch (Exception ignored) {
      return null;
    }
  }
}
