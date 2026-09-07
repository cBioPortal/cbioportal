package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.cbioportal.legacy.model.ClinicalEventData;

@Schema(name = "ClinicalEvent", description = "Represents a clinical event")
public record ClinicalEventDTO(
    String studyId,
    String patientId,
    String uniquePatientKey,
    String eventType,
    Integer startNumberOfDaysSinceDiagnosis,
    Integer endNumberOfDaysSinceDiagnosis,
    List<ClinicalEventData> attributes) {}
