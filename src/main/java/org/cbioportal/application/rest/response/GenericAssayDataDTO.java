package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GenericAssayData", description = "Represents a generic assay value of a sample")
public record GenericAssayDataDTO(
    String uniqueSampleKey,
    String uniquePatientKey,
    String molecularProfileId,
    String sampleId,
    String patientId,
    String studyId,
    String value,
    String genericAssayStableId,
    boolean patientLevel,
    String stableId) {}
