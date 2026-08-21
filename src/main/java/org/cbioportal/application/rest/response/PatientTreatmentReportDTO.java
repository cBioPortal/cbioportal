package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.cbioportal.legacy.model.PatientTreatment;

@Schema(
    name = "PatientTreatmentReport",
    description = "Represents a patient-level treatment report")
public record PatientTreatmentReportDTO(
    Integer totalPatients, Integer totalSamples, List<PatientTreatment> patientTreatments) {}
