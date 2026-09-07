package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import org.cbioportal.legacy.model.SampleTreatmentRow;

@Schema(name = "SampleTreatmentReport", description = "Represents a sample-level treatment report")
public record SampleTreatmentReportDTO(
    Integer totalSamples, Collection<SampleTreatmentRow> treatments) {}
