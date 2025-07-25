package org.cbioportal.application.rest.response;

import org.cbioportal.domain.sample.SampleType;

public record SampleDTO(
    String sampleId,
    SampleType sampleType,
    String patientId,
    String studyId,
    Boolean sequenced,
    Boolean copyNumberSegmentPresent,
    String uniqueSampleKey,
    String uniquePatientKey) {}
