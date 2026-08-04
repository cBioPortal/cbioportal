package org.cbioportal.application.rest;

import org.cbioportal.domain.wsi.WsiHierarchy;
import org.cbioportal.domain.wsi.repository.WsiHierarchyRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Serves the materialized WSI hierarchy from the cBioPortal ClickHouse store. */
@RestController
@RequestMapping("/api/wsi/v2/hierarchy")
public class WsiHierarchyController {

  private final WsiHierarchyRepository repository;

  public WsiHierarchyController(WsiHierarchyRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/{studyId}/{patientId}")
  @PreAuthorize(
      "isFullyAuthenticated() and hasPermission(#studyId, 'CancerStudyId', "
          + "T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<WsiHierarchy> getPatientHierarchy(
      @PathVariable String studyId, @PathVariable String patientId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
          .header(HttpHeaders.VARY, "Authorization, Cookie")
          .build();
    }
    WsiHierarchy hierarchy = repository.getPatientHierarchy(studyId, patientId);
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
}
