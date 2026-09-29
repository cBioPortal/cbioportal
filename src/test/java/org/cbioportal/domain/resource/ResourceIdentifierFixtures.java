package org.cbioportal.domain.resource;

import java.util.Arrays;
import java.util.List;
import org.cbioportal.legacy.web.parameter.PatientIdentifier;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;

/** Builds the (studyId, stableId) identifier pairs the resource table requests take. */
public final class ResourceIdentifierFixtures {

  private ResourceIdentifierFixtures() {}

  public static List<PatientIdentifier> patients(String studyId, String... patientIds) {
    return Arrays.stream(patientIds)
        .map(
            patientId -> {
              PatientIdentifier identifier = new PatientIdentifier();
              identifier.setStudyId(studyId);
              identifier.setPatientId(patientId);
              return identifier;
            })
        .toList();
  }

  public static List<SampleIdentifier> samples(String studyId, String... sampleIds) {
    return Arrays.stream(sampleIds)
        .map(
            sampleId -> {
              SampleIdentifier identifier = new SampleIdentifier();
              identifier.setStudyId(studyId);
              identifier.setSampleId(sampleId);
              return identifier;
            })
        .toList();
  }
}
