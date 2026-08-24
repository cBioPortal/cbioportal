package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MutationSpectrum", description = "Represents mutation spectrum counts")
public record MutationSpectrumDTO(
    String uniqueSampleKey,
    String uniquePatientKey,
    String molecularProfileId,
    String sampleId,
    String patientId,
    String studyId,
    Integer CtoA,
    Integer CtoG,
    Integer CtoT,
    Integer TtoA,
    Integer TtoC,
    Integer TtoG) {}
