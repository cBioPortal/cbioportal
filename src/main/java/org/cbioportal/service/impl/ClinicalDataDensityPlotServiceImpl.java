package org.cbioportal.service.impl;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DensityPlotBin;
import org.cbioportal.model.DensityPlotData;
import org.cbioportal.service.ClinicalDataDensityPlotService;
import org.cbioportal.web.columnar.StudyViewColumnStoreController;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.DensityPlotParameters;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ClinicalDataDensityPlotServiceImpl implements ClinicalDataDensityPlotService {

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)"
    )
    @Override
    public DensityPlotData getDensityPlotData(List<ClinicalData> sampleClinicalData, DensityPlotParameters densityPlotParameters, StudyViewFilter studyViewFilter) {
        DensityPlotData result = new DensityPlotData();
        result.setBins(new ArrayList<>());

        Map<String, List<ClinicalData>> clinicalDataGroupedBySampleId = sampleClinicalData.stream().
            collect(Collectors.groupingBy(c -> c.getStudyId() + "_" + c.getSampleId()));

        List<ClinicalData> extractedXYClinicalData = clinicalDataGroupedBySampleId.entrySet().stream()
            .filter(entry -> entry.getValue().size() == 2 &&
                NumberUtils.isCreatable(entry.getValue().get(0).getAttrValue()) &&
                NumberUtils.isCreatable(entry.getValue().get(1).getAttrValue())
            ).flatMap(entry -> entry.getValue().stream())
            .toList();
        
        if (extractedXYClinicalData.isEmpty()) {
            return result;
        }
        
        Map<Boolean, List<ClinicalData>> partition = extractedXYClinicalData.stream().collect(
            Collectors.partitioningBy(c -> c.getAttrId().equals(densityPlotParameters.getXAxisAttributeId())));

        boolean useXLogScale = densityPlotParameters.getXAxisLogScale() && ClinicalDataDensityPlotServiceImpl.isLogScalePossibleForAttribute(densityPlotParameters.getXAxisAttributeId());
        boolean useYLogScale = densityPlotParameters.getYAxisLogScale() && ClinicalDataDensityPlotServiceImpl.isLogScalePossibleForAttribute(densityPlotParameters.getYAxisAttributeId());

        double[] xValues = partition.get(true).stream().mapToDouble(
            useXLogScale ? ClinicalDataDensityPlotServiceImpl::parseValueLog : ClinicalDataDensityPlotServiceImpl::parseValueLinear
        ).toArray();
        double[] yValues = partition.get(false).stream().mapToDouble(
            useYLogScale ? ClinicalDataDensityPlotServiceImpl::parseValueLog : ClinicalDataDensityPlotServiceImpl::parseValueLinear
        ).toArray();
        double[] xValuesCopy = Arrays.copyOf(xValues, xValues.length);
        double[] yValuesCopy = Arrays.copyOf(yValues, yValues.length); // Why copy these?
        Arrays.sort(xValuesCopy);
        Arrays.sort(yValuesCopy);

        double xAxisStartValue = densityPlotParameters.getXAxisStart() == null ? xValuesCopy[0] :
            (useXLogScale ? ClinicalDataDensityPlotServiceImpl.logScale(densityPlotParameters.getXAxisStart().doubleValue()) : densityPlotParameters.getXAxisStart().doubleValue());
        double xAxisEndValue = densityPlotParameters.getXAxisEnd() == null ? xValuesCopy[xValuesCopy.length - 1] :
            (useXLogScale ? ClinicalDataDensityPlotServiceImpl.logScale(densityPlotParameters.getXAxisEnd().doubleValue()) : densityPlotParameters.getXAxisEnd().doubleValue());
        double yAxisStartValue = densityPlotParameters.getYAxisStart() == null ? yValuesCopy[0] :
            (useYLogScale ? ClinicalDataDensityPlotServiceImpl.logScale(densityPlotParameters.getYAxisStart().doubleValue()) : densityPlotParameters.getYAxisStart().doubleValue());
        double yAxisEndValue = densityPlotParameters.getYAxisEnd() == null ? yValuesCopy[yValuesCopy.length - 1] :
            (useYLogScale ? ClinicalDataDensityPlotServiceImpl.logScale(densityPlotParameters.getYAxisEnd().doubleValue()) : densityPlotParameters.getYAxisEnd().doubleValue());
        double xAxisBinInterval = (xAxisEndValue - xAxisStartValue) / densityPlotParameters.getXAxisBinCount();
        double yAxisBinInterval = (yAxisEndValue - yAxisStartValue) / densityPlotParameters.getYAxisBinCount();
        List<DensityPlotBin> bins = result.getBins();
        for (int i = 0; i < densityPlotParameters.getXAxisBinCount(); i++) {
            for (int j = 0; j < densityPlotParameters.getYAxisBinCount(); j++) {
                DensityPlotBin densityPlotBin = new DensityPlotBin();
                densityPlotBin.setBinX(BigDecimal.valueOf(xAxisStartValue + (i * xAxisBinInterval)));
                densityPlotBin.setBinY(BigDecimal.valueOf(yAxisStartValue + (j * yAxisBinInterval)));
                densityPlotBin.setCount(0);
                bins.add(densityPlotBin);
            }
        }

        for (int i = 0; i < xValues.length; i++) {
            double xValue = xValues[i];
            double yValue = yValues[i];
            int xBinIndex = (int) ((xValue - xAxisStartValue) / xAxisBinInterval);
            int yBinIndex = (int) ((yValue - yAxisStartValue) / yAxisBinInterval);
            int index = (int) (((xBinIndex - (xBinIndex == densityPlotParameters.getXAxisBinCount() ? 1 : 0)) * densityPlotParameters.getYAxisBinCount()) +
                (yBinIndex - (yBinIndex == densityPlotParameters.getYAxisBinCount() ? 1 : 0)));
            DensityPlotBin densityPlotBin = bins.get(index);
            densityPlotBin.setCount(densityPlotBin.getCount() + 1);
            BigDecimal xValueBigDecimal = BigDecimal.valueOf(xValue);
            BigDecimal yValueBigDecimal = BigDecimal.valueOf(yValue);
            
            // Set new min and max as needed
            if (densityPlotBin.getMinX() == null || densityPlotBin.getMinX().compareTo(xValueBigDecimal) > 0){
                densityPlotBin.setMinX(xValueBigDecimal);
            }
            if (densityPlotBin.getMaxX() == null || densityPlotBin.getMaxX().compareTo(xValueBigDecimal) < 0){
                densityPlotBin.setMaxX(xValueBigDecimal);
            }
            if (densityPlotBin.getMinY() == null || densityPlotBin.getMinY().compareTo(yValueBigDecimal) > 0){
                densityPlotBin.setMinY(yValueBigDecimal);
            }
            if (densityPlotBin.getMaxY() == null || densityPlotBin.getMaxY().compareTo(yValueBigDecimal) < 0){
                densityPlotBin.setMaxY(yValueBigDecimal);
            }
        }

        if (xValues.length > 1) {
            // need at least 2 entries in each to compute correlation
            result.setPearsonCorr(new PearsonsCorrelation().correlation(xValues, yValues));
            result.setSpearmanCorr(new SpearmansCorrelation().correlation(xValues, yValues));
        } else {
            // if less than 1 entry, just set 0 correlation
            result.setSpearmanCorr(0.0);
            result.setPearsonCorr(0.0);
        }

        // filter out empty bins
        result.setBins(result.getBins().stream().filter((bin)->(bin.getCount() > 0)).collect(Collectors.toList()));
        return result;
    }


    private static boolean isLogScalePossibleForAttribute(String clinicalAttributeId) {
        return clinicalAttributeId.equals("MUTATION_COUNT");
    }

    private static double logScale(double val) {
        return Math.log(1+val);
    }

    private static double parseValueLog(ClinicalData c) {
        return ClinicalDataDensityPlotServiceImpl.logScale(Double.parseDouble(c.getAttrValue()));
    }

    private static double parseValueLinear(ClinicalData c) {
        return Double.parseDouble(c.getAttrValue());
    }
}
