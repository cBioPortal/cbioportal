package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.cbioportal.legacy.model.DensityPlotBin;

@Schema(name = "DensityPlotData", description = "Represents density plot data")
public record DensityPlotDataDTO(
    List<DensityPlotBin> bins, Double pearsonCorr, Double spearmanCorr) {}
