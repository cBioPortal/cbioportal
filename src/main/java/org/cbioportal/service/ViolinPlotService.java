package org.cbioportal.service;

import java.math.BigDecimal;
import java.util.List;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalViolinPlotData;
import org.cbioportal.model.Sample;

public interface ViolinPlotService {
  ClinicalViolinPlotData getClinicalViolinPlotData(
      List<ClinicalData> sampleClinicalDataForViolinPlot,
      List<Sample> samplesForSampleCounts,
      BigDecimal axisStart,
      BigDecimal axisEnd,
      BigDecimal numCurvePoints,
      Boolean useLogScale,
      BigDecimal sigmaMultiplier);
}
