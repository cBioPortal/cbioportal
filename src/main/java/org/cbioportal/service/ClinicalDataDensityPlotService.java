package org.cbioportal.service;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DensityPlotData;

import java.math.BigDecimal;
import java.util.List;

public interface ClinicalDataDensityPlotService {
    DensityPlotData getDensityPlotData(List<ClinicalData> filteredClinicalData,
                                       String xAxisAttributeId,
                                       String yAxisAttributeId,
                                       Boolean xAxisLogScale,
                                       Boolean yAxisLogScale,
                                       Integer xAxisBinCount,
                                       Integer yAxisBinCount,
                                       BigDecimal xAxisStart,
                                       BigDecimal yAxisStart,
                                       BigDecimal xAxisEnd,
                                       BigDecimal yAxisEnd);
    
    List<ClinicalData> filterClinicalData(List<ClinicalData> clinicalDataList);
}
