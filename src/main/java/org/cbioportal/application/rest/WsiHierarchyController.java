package org.cbioportal.application.rest;

import org.cbioportal.domain.wsi.WsiHierarchy;
import org.cbioportal.domain.wsi.repository.WsiHierarchyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

  @Value("${wsi.local-auth-bypass:false}")
  private boolean localAuthBypass;

  public WsiHierarchyController(WsiHierarchyRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/{studyId}/{patientId}")
  @PreAuthorize(
      "!isAuthenticated() or hasPermission(#studyId, 'CancerStudyId', "
          + "T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<WsiHierarchy> getPatientHierarchy(
      @PathVariable String studyId, @PathVariable String patientId) {
    if (WsiResponses.isAnonymous(SecurityContextHolder.getContext().getAuthentication())
        && !localAuthBypass) {
      return WsiResponses.privateResponse(HttpStatus.UNAUTHORIZED).build();
    }

    WsiHierarchy hierarchy = repository.getPatientHierarchy(studyId, patientId);
    if (hierarchy == null) {
      return WsiResponses.privateResponse(HttpStatus.NOT_FOUND).build();
    }
    return WsiResponses.privateResponse(HttpStatus.OK)
        .contentType(MediaType.APPLICATION_JSON)
        .body(hierarchy);
  }
}
