package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(
    name = "NumericGeneMolecularData",
    description = "Represents a numeric gene-level molecular measurement of a sample")
public record NumericGeneMolecularDataDTO(
    String uniqueSampleKey,
    String uniquePatientKey,
    Integer entrezGeneId,
    GeneDTO gene,
    String molecularProfileId,
    String sampleId,
    String patientId,
    String studyId,
    BigDecimal value) {}
