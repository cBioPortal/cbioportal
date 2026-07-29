package org.cbioportal.application.rest;

import org.cbioportal.domain.wsi.repository.WsiHierarchyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Serves the materialized WSI hierarchy from the cBioPortal ClickHouse store. */
@RestController
@RequestMapping("/api/wsi/hierarchy")
public class WsiHierarchyController {

  private final WsiHierarchyRepository repository;

  public WsiHierarchyController(WsiHierarchyRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/{studyId}/{patientId}")
  @PreAuthorize(
      "hasPermission(#studyId, 'CancerStudyId', "
          + "T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<String> getPatientHierarchy(
      @PathVariable String studyId, @PathVariable String patientId) {
    String hierarchy = repository.getPatientHierarchy(studyId, patientId);
    if (hierarchy == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(hierarchy);
  }

  @GetMapping("/{studyId}/{patientId}/bootstrap")
  @PreAuthorize(
      "hasPermission(#studyId, 'CancerStudyId', "
          + "T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  public ResponseEntity<String> getPatientHierarchyBootstrap(
      @PathVariable String studyId, @PathVariable String patientId) {
    String hierarchy = repository.getPatientHierarchy(studyId, patientId);
    if (hierarchy == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body("{\"hierarchy\":" + hierarchy + ",\"initial\":null}");
  }
}
