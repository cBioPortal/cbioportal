package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cbioportal.domain.sample.SampleType;

@Schema(name = "Sample", description = "Represents a sample")
public record SampleDTO(
    String sampleId,
    SampleType sampleType,
    String patientId,
    String studyId,
    Boolean sequenced,
    Boolean copyNumberSegmentPresent,
    String uniqueSampleKey,
    String uniquePatientKey) {}
