package org.cbioportal.application.rest.availability;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.cbioportal.application.file.export.ExportController;
import org.cbioportal.application.seo.SitemapController;
import org.cbioportal.legacy.web.StudyController;
import org.junit.jupiter.api.Test;
import org.springframework.aop.ClassFilter;

class StudyAvailabilityConfigTest {

  private final ClassFilter classFilter =
      new StudyAvailabilityConfig.RestEndpointPointcut().getClassFilter();

  @Test
  void coversStudyScopedControllers() {
    assertTrue(classFilter.matches(StudyController.class));
    assertTrue(classFilter.matches(ExportController.class));
    assertTrue(classFilter.matches(SitemapController.class));
  }

  @Test
  void matchesPackagesOnSegmentBoundaries() {
    assertTrue(
        StudyAvailabilityConfig.isInPackage(
            "org.cbioportal.legacy.web", "org.cbioportal.legacy.web"));
    assertTrue(
        StudyAvailabilityConfig.isInPackage(
            "org.cbioportal.legacy.web.parameter", "org.cbioportal.legacy.web"));
    assertFalse(
        StudyAvailabilityConfig.isInPackage(
            "org.cbioportal.legacy.webhooks", "org.cbioportal.legacy.web"));
  }
}
