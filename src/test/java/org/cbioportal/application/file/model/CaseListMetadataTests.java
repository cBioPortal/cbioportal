package org.cbioportal.application.file.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CaseListMetadataTests {
  @Test
  public void shouldRemoveOnlyLeadingStudyIdPrefix() {
    CaseListMetadata metadata = new CaseListMetadata();
    metadata.setCancerStudyIdentifier("study");
    metadata.setStableId("study_study_mutations");

    assertEquals("study_mutations", metadata.getCaseListTypeStableId());
  }

  @Test
  public void shouldRemoveOnlyLeadingStudyIdPrefixFromGeneticProfile() {
    GeneticProfileDatatypeMetadata metadata = new GeneticProfileDatatypeMetadata();
    metadata.setCancerStudyIdentifier("study");
    metadata.setStableId("study_study_mutations");

    assertEquals("study_mutations", metadata.getGeneticDatatypeStableId());
  }
}
