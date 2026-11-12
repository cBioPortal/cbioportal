package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "GenePanelData",
    description = "Gene panel and profiled status of a sample in a molecular profile")
public record GenePanelDataDTO(
    String uniqueSampleKey,
    String uniquePatientKey,
    String molecularProfileId,
    String sampleId,
    String patientId,
    String studyId,
    String genePanelId,
    Boolean profiled) {}
