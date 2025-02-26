package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.DensityPlotData;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.legacy.web.util.DensityPlotParameters;

import java.math.BigDecimal;
import java.util.List;

public interface ClinicalDataDensityPlotService {
    DensityPlotData getDensityPlotData(List<ClinicalData> filteredClinicalData, DensityPlotParameters densityPlotParameters, StudyViewFilter studyViewFilter);
}
