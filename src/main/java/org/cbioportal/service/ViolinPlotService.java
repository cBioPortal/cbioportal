package org.cbioportal.service;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalViolinPlotData;
import org.cbioportal.model.Sample;
import org.cbioportal.web.parameter.StudyViewFilter;

import java.math.BigDecimal;
import java.util.List;

public interface ViolinPlotService {
    ClinicalViolinPlotData getClinicalViolinPlotData(
        List<ClinicalData> sampleClinicalDataForViolinPlot,
        List<Sample> samplesForSampleCounts,
        BigDecimal axisStart,
        BigDecimal axisEnd,
        BigDecimal numCurvePoints,
        Boolean useLogScale,
        BigDecimal sigmaMultiplier,
        StudyViewFilter studyViewFilter
    );
}
