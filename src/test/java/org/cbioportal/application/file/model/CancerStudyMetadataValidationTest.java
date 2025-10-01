package org.cbioportal.application.file.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CancerStudyMetadataValidationTest {

  private Validator validator;

  @Before
  public void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void testValidStudyIdentifierWithAlphanumeric() {
    CancerStudyMetadata metadata = new CancerStudyMetadata();
    metadata.setCancerStudyIdentifier("study123");

    Set<ConstraintViolation<CancerStudyMetadata>> violations = validator.validate(metadata);
    Assert.assertTrue(
        "Study identifier with alphanumeric characters should be valid", violations.isEmpty());
  }

  @Test
  public void testValidStudyIdentifierWithUnderscore() {
    CancerStudyMetadata metadata = new CancerStudyMetadata();
    metadata.setCancerStudyIdentifier("study_es_0");

    Set<ConstraintViolation<CancerStudyMetadata>> violations = validator.validate(metadata);
    Assert.assertTrue(
        "Study identifier with underscores should be valid", violations.isEmpty());
  }

  @Test
  public void testValidStudyIdentifierWithHyphen() {
    CancerStudyMetadata metadata = new CancerStudyMetadata();
    metadata.setCancerStudyIdentifier("study-es-0");

    Set<ConstraintViolation<CancerStudyMetadata>> violations = validator.validate(metadata);
    Assert.assertTrue("Study identifier with hyphens should be valid", violations.isEmpty());
  }

  @Test
  public void testValidStudyIdentifierWithMixedCharacters() {
    CancerStudyMetadata metadata = new CancerStudyMetadata();
    metadata.setCancerStudyIdentifier("Study_123-ABC");

    Set<ConstraintViolation<CancerStudyMetadata>> violations = validator.validate(metadata);
    Assert.assertTrue(
        "Study identifier with mixed valid characters should be valid", violations.isEmpty());
  }

  @Test
  public void testInvalidStudyIdentifierWithPlus() {
    CancerStudyMetadata metadata = new CancerStudyMetadata();
    metadata.setCancerStudyIdentifier("study+es+0");

    Set<ConstraintViolation<CancerStudyMetadata>> violations = validator.validate(metadata);
    Assert.assertFalse(
        "Study identifier with plus signs should be invalid", violations.isEmpty());
    Assert.assertTrue(
        "Error message should mention allowed characters",
        violations.iterator().next().getMessage().contains("alphanumeric"));
  }

  @Test
  public void testInvalidStudyIdentifierWithSpace() {
    CancerStudyMetadata metadata = new CancerStudyMetadata();
    metadata.setCancerStudyIdentifier("study es 0");

    Set<ConstraintViolation<CancerStudyMetadata>> violations = validator.validate(metadata);
    Assert.assertFalse("Study identifier with spaces should be invalid", violations.isEmpty());
  }

  @Test
  public void testInvalidStudyIdentifierWithSpecialCharacters() {
    CancerStudyMetadata metadata = new CancerStudyMetadata();
    metadata.setCancerStudyIdentifier("study@es#0");

    Set<ConstraintViolation<CancerStudyMetadata>> violations = validator.validate(metadata);
    Assert.assertFalse(
        "Study identifier with special characters should be invalid", violations.isEmpty());
  }

  @Test
  public void testInvalidStudyIdentifierWithPercent() {
    CancerStudyMetadata metadata = new CancerStudyMetadata();
    metadata.setCancerStudyIdentifier("study%20");

    Set<ConstraintViolation<CancerStudyMetadata>> violations = validator.validate(metadata);
    Assert.assertFalse(
        "Study identifier with percent encoding should be invalid", violations.isEmpty());
  }
}
