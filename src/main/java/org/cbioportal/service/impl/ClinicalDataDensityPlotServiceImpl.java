package org.cbioportal.service.impl;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DensityPlotBin;
import org.cbioportal.model.DensityPlotData;
import org.cbioportal.service.ClinicalDataDensityPlotService;
import org.cbioportal.web.columnar.StudyViewColumnStoreController;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClinicalDataDensityPlotServiceImpl implements ClinicalDataDensityPlotService {
    @Override
    public DensityPlotData getDensityPlotData(List<ClinicalData> filteredClinicalData, 
                                              String xAxisAttributeId, 
                                              String yAxisAttributeId, 
                                              Boolean xAxisLogScale,
                                              Boolean yAxisLogScale,
                                              Integer xAxisBinCount,
                                              Integer yAxisBinCount,
                                              BigDecimal xAxisStart,
                                              BigDecimal yAxisStart,
                                              BigDecimal xAxisEnd,
                                              BigDecimal yAxisEnd) {
        DensityPlotData result = new DensityPlotData();
        result.setBins(new ArrayList<>());
        
        if (filteredClinicalData.isEmpty()) {
            return result;
        }
        
        Map<Boolean, List<ClinicalData>> partition = filteredClinicalData.stream().collect(
            Collectors.partitioningBy(c -> c.getAttrId().equals(xAxisAttributeId)));

        boolean useXLogScale = xAxisLogScale && ClinicalDataDensityPlotServiceImpl.isLogScalePossibleForAttribute(xAxisAttributeId);
        boolean useYLogScale = yAxisLogScale && ClinicalDataDensityPlotServiceImpl.isLogScalePossibleForAttribute(yAxisAttributeId);

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

        double xAxisStartValue = xAxisStart == null ? xValuesCopy[0] :
            (useXLogScale ? ClinicalDataDensityPlotServiceImpl.logScale(xAxisStart.doubleValue()) : xAxisStart.doubleValue());
        double xAxisEndValue = xAxisEnd == null ? xValuesCopy[xValuesCopy.length - 1] :
            (useXLogScale ? ClinicalDataDensityPlotServiceImpl.logScale(xAxisEnd.doubleValue()) : xAxisEnd.doubleValue());
        double yAxisStartValue = yAxisStart == null ? yValuesCopy[0] :
            (useYLogScale ? ClinicalDataDensityPlotServiceImpl.logScale(yAxisStart.doubleValue()) : yAxisStart.doubleValue());
        double yAxisEndValue = yAxisEnd == null ? yValuesCopy[yValuesCopy.length - 1] :
            (useYLogScale ? ClinicalDataDensityPlotServiceImpl.logScale(yAxisEnd.doubleValue()) : yAxisEnd.doubleValue());
        double xAxisBinInterval = (xAxisEndValue - xAxisStartValue) / xAxisBinCount;
        double yAxisBinInterval = (yAxisEndValue - yAxisStartValue) / yAxisBinCount;
        List<DensityPlotBin> bins = result.getBins();
        for (int i = 0; i < xAxisBinCount; i++) {
            for (int j = 0; j < yAxisBinCount; j++) {
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
            int index = (int) (((xBinIndex - (xBinIndex == xAxisBinCount ? 1 : 0)) * yAxisBinCount) +
                (yBinIndex - (yBinIndex == yAxisBinCount ? 1 : 0)));
            DensityPlotBin densityPlotBin = bins.get(index);
            densityPlotBin.setCount(densityPlotBin.getCount() + 1);
            BigDecimal xValueBigDecimal = BigDecimal.valueOf(xValue);
            BigDecimal yValueBigDecimal = BigDecimal.valueOf(yValue);
            if (densityPlotBin.getMinX() != null) {
                if (densityPlotBin.getMinX().compareTo(xValueBigDecimal) > 0) {
                    densityPlotBin.setMinX(xValueBigDecimal);
                }
            } else {
                densityPlotBin.setMinX(xValueBigDecimal);
            }
            if (densityPlotBin.getMaxX() != null) {
                if (densityPlotBin.getMaxX().compareTo(xValueBigDecimal) < 0) {
                    densityPlotBin.setMaxX(xValueBigDecimal);
                }
            } else {
                densityPlotBin.setMaxX(xValueBigDecimal);
            }
            if (densityPlotBin.getMinY() != null) {
                if (densityPlotBin.getMinY().compareTo(yValueBigDecimal) > 0) {
                    densityPlotBin.setMinY(yValueBigDecimal);
                }
            } else {
                densityPlotBin.setMinY(yValueBigDecimal);
            }
            if (densityPlotBin.getMaxY() != null) {
                if (densityPlotBin.getMaxY().compareTo(yValueBigDecimal) < 0) {
                    densityPlotBin.setMaxY(yValueBigDecimal);
                }
            } else {
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

    @Override
    public List<ClinicalData> filterClinicalData(List<ClinicalData> clinicalDataList) {
        Map<String, List<ClinicalData>> clinicalDataGroupedBySampleId = clinicalDataList.stream().
            collect(Collectors.groupingBy(ClinicalData::getSampleId));

        return clinicalDataGroupedBySampleId.entrySet().stream()
            .filter(entry -> entry.getValue().size() == 2 &&
                NumberUtils.isCreatable(entry.getValue().get(0).getAttrValue()) &&
                NumberUtils.isCreatable(entry.getValue().get(1).getAttrValue())
            ).flatMap(entry -> entry.getValue().stream())
            .toList();
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
