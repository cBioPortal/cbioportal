package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(
    name = "DiscreteCopyNumberData",
    description = "Represents a discrete copy number alteration")
public record DiscreteCopyNumberDataDTO(
    String uniqueSampleKey,
    String uniquePatientKey,
    String molecularProfileId,
    String sampleId,
    String patientId,
    Integer entrezGeneId,
    GeneDTO gene,
    String studyId,
    String driverFilter,
    String driverFilterAnnotation,
    String driverTiersFilter,
    String driverTiersFilterAnnotation,
    Integer alteration,
    Map<String, Map<String, Object>> namespaceColumns) {}
