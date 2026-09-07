package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cbioportal.legacy.model.Geneset;

@Schema(name = "GenesetMolecularData", description = "Represents geneset molecular data")
public record GenesetMolecularDataDTO(
    String geneticProfileId,
    String sampleId,
    String patientId,
    String studyId,
    String value,
    String genesetId,
    Geneset geneset,
    String uniqueSampleKey,
    String uniquePatientKey) {}
