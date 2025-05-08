package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.DensityPlotData;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.legacy.web.util.DensityPlotParameters;

public interface ClinicalDataDensityPlotService {
  DensityPlotData getDensityPlotData(
      List<ClinicalData> filteredClinicalData,
      DensityPlotParameters densityPlotParameters,
      StudyViewFilter studyViewFilter);
}
