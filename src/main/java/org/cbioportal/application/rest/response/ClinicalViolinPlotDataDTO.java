package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.cbioportal.legacy.model.ClinicalViolinPlotRowData;

@Schema(name = "ClinicalViolinPlotData", description = "Represents clinical violin plot data")
public record ClinicalViolinPlotDataDTO(
    List<ClinicalViolinPlotRowData> rows, Double axisStart, Double axisEnd) {}
