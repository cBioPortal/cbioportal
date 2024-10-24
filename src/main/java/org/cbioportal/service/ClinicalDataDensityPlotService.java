package org.cbioportal.service;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DensityPlotData;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.DensityPlotParameters;

import java.math.BigDecimal;
import java.util.List;

public interface ClinicalDataDensityPlotService {
    DensityPlotData getDensityPlotData(List<ClinicalData> filteredClinicalData, DensityPlotParameters densityPlotParameters, StudyViewFilter studyViewFilter);
}
