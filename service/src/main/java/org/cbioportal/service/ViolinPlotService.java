package org.cbioportal.service;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalViolinPlotData;

import java.math.BigDecimal;
import java.util.List;

public interface ViolinPlotService {
    ClinicalViolinPlotData getClinicalViolinPlotData(
        List<ClinicalData> sampleClinicalData,
        BigDecimal axisStart,
        BigDecimal axisEnd,
        BigDecimal numCurvePoints,
        Boolean useLogScale,
        BigDecimal sigmaMultiplier
    );
}
