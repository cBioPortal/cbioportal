package org.cbioportal.legacy.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CancerStudyValidationTest {

  private Validator validator;

  @Before
  public void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void testValidStudyIdentifierWithAlphanumeric() {
    CancerStudy cancerStudy = new CancerStudy();
    cancerStudy.setCancerStudyIdentifier("study123");

    Set<ConstraintViolation<CancerStudy>> violations = validator.validate(cancerStudy);
    Assert.assertTrue(
        "Study identifier with alphanumeric characters should be valid", violations.isEmpty());
  }

  @Test
  public void testValidStudyIdentifierWithUnderscore() {
    CancerStudy cancerStudy = new CancerStudy();
    cancerStudy.setCancerStudyIdentifier("study_es_0");

    Set<ConstraintViolation<CancerStudy>> violations = validator.validate(cancerStudy);
    Assert.assertTrue(
        "Study identifier with underscores should be valid", violations.isEmpty());
  }

  @Test
  public void testValidStudyIdentifierWithHyphen() {
    CancerStudy cancerStudy = new CancerStudy();
    cancerStudy.setCancerStudyIdentifier("study-es-0");

    Set<ConstraintViolation<CancerStudy>> violations = validator.validate(cancerStudy);
    Assert.assertTrue("Study identifier with hyphens should be valid", violations.isEmpty());
  }

  @Test
  public void testValidStudyIdentifierWithMixedCharacters() {
    CancerStudy cancerStudy = new CancerStudy();
    cancerStudy.setCancerStudyIdentifier("Study_123-ABC");

    Set<ConstraintViolation<CancerStudy>> violations = validator.validate(cancerStudy);
    Assert.assertTrue(
        "Study identifier with mixed valid characters should be valid", violations.isEmpty());
  }

  @Test
  public void testInvalidStudyIdentifierWithPlus() {
    CancerStudy cancerStudy = new CancerStudy();
    cancerStudy.setCancerStudyIdentifier("study+es+0");

    Set<ConstraintViolation<CancerStudy>> violations = validator.validate(cancerStudy);
    Assert.assertFalse(
        "Study identifier with plus signs should be invalid", violations.isEmpty());
    Assert.assertTrue(
        "Error message should mention allowed characters",
        violations.iterator().next().getMessage().contains("alphanumeric"));
  }

  @Test
  public void testInvalidStudyIdentifierWithSpace() {
    CancerStudy cancerStudy = new CancerStudy();
    cancerStudy.setCancerStudyIdentifier("study es 0");

    Set<ConstraintViolation<CancerStudy>> violations = validator.validate(cancerStudy);
    Assert.assertFalse("Study identifier with spaces should be invalid", violations.isEmpty());
  }

  @Test
  public void testInvalidStudyIdentifierWithSpecialCharacters() {
    CancerStudy cancerStudy = new CancerStudy();
    cancerStudy.setCancerStudyIdentifier("study@es#0");

    Set<ConstraintViolation<CancerStudy>> violations = validator.validate(cancerStudy);
    Assert.assertFalse(
        "Study identifier with special characters should be invalid", violations.isEmpty());
  }

  @Test
  public void testInvalidStudyIdentifierWithPercent() {
    CancerStudy cancerStudy = new CancerStudy();
    cancerStudy.setCancerStudyIdentifier("study%20");

    Set<ConstraintViolation<CancerStudy>> violations = validator.validate(cancerStudy);
    Assert.assertFalse(
        "Study identifier with percent encoding should be invalid", violations.isEmpty());
  }

  @Test
  public void testNullStudyIdentifier() {
    CancerStudy cancerStudy = new CancerStudy();
    cancerStudy.setCancerStudyIdentifier(null);

    Set<ConstraintViolation<CancerStudy>> violations = validator.validate(cancerStudy);
    Assert.assertFalse("Null study identifier should be invalid", violations.isEmpty());
  }
}
