package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalViolinPlotData;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface ViolinPlotService {
    ClinicalViolinPlotData getClinicalViolinPlotData(
        List<ClinicalData> sampleClinicalDataForViolinPlot,
        Set<Integer> samplesForSampleCountsIds,
        BigDecimal axisStart,
        BigDecimal axisEnd,
        BigDecimal numCurvePoints,
        Boolean useLogScale,
        BigDecimal sigmaMultiplier,
        StudyViewFilter studyViewFilter
    );
}
