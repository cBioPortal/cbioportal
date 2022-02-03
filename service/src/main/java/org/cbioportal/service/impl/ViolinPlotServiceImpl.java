package org.cbioportal.service.impl;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.cbioportal.model.*;
import org.cbioportal.service.ViolinPlotService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ViolinPlotServiceImpl implements ViolinPlotService {
    // If a row has less than this many points, do not compute a
    //  violin, because it doesn't make sense.
    static final int SHOW_ONLY_POINTS_THRESHOLD = 7;
    
    public ClinicalViolinPlotData getClinicalViolinPlotData(
        List<ClinicalData> sampleClinicalData,
        BigDecimal axisStart,
        BigDecimal axisEnd,
        BigDecimal numCurvePoints,
        Boolean useLogScale,
        BigDecimal sigmaMultiplier
    ) {
        ClinicalViolinPlotData result = new ClinicalViolinPlotData();
        result.setAxisStart(Double.POSITIVE_INFINITY);
        result.setAxisEnd(Double.NEGATIVE_INFINITY);
        result.setRows(new ArrayList<>());
        
        // clinicalDataMap is a map sampleId->studyId->data
        Map<String, Map<String, List<ClinicalData>>> clinicalDataMap = sampleClinicalData.stream()
            .collect(Collectors.groupingBy(ClinicalData::getSampleId, Collectors.groupingBy(ClinicalData::getStudyId)));

        // Group data by category
        Map<String, List<ClinicalData>> groupedDetailedData = new HashMap<>();
        clinicalDataMap.forEach((studyId, dataBySampleId) -> dataBySampleId.forEach((sampleId, sampleData) -> {
            // sampleData.size() == 2 means we have clinical data for the sample for both of the queried attributes
            // We also ensure that the second data is numerical, as expected
            if (sampleData.size() == 2) {
                int numericalIndex = 0;
                int categoricalIndex = 1;
                if (NumberUtils.isCreatable(sampleData.get(1).getAttrValue())) {
                    numericalIndex = 1;
                    categoricalIndex = 0;
                }
                String category = sampleData.get(categoricalIndex).getAttrValue();
                ClinicalData datum = sampleData.get(numericalIndex);
                if (!groupedDetailedData.containsKey(category)) {
                    groupedDetailedData.put(category, new ArrayList<>());
                }
                groupedDetailedData.get(category).add(datum);
            }
        }));

        if (groupedDetailedData.isEmpty()) {
            return result;
        }

        // Calculate boxes, outliers, and data bounds
        Map<String, ClinicalViolinPlotBoxData> boxData = new HashMap<>();
        Map<String, List<ClinicalData>> nonOutliers = new HashMap<>();
        Map<String, List<ClinicalData>> outliers = new HashMap<>();
        groupedDetailedData.forEach((category, data)->{
            Percentile percentile = new Percentile();
            // fill double[] to pass into Percentile
            double[] values = new double[data.size()];
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            int valuesIndex = 0;
            for (ClinicalData d: data) {
                Double value = useLogScale ? ViolinPlotServiceImpl.logScale(Double.parseDouble(d.getAttrValue())) : Double.parseDouble(d.getAttrValue());
                values[valuesIndex] = value;
                min = Math.min(value, min);
                max = Math.max(value, max);
                valuesIndex += 1;
            }
            
            percentile.setData(values);

            double q1 = percentile.evaluate(25);
            double q3 = percentile.evaluate(75);
            double IQR = q3 - q1;
            double SUSPECTED_OUTLIER_MULTIPLE = 1.5;
            double OUTLIER_MULTIPLE = 3;
            double outlierThresholdLower = q1 - OUTLIER_MULTIPLE * IQR;
            double outlierThresholdUpper = q3 + OUTLIER_MULTIPLE * IQR;
            double suspectedOutlierThresholdLower = q1 - SUSPECTED_OUTLIER_MULTIPLE * IQR;
            double suspectedOutlierThresholdUpper = q3 + SUSPECTED_OUTLIER_MULTIPLE * IQR;

            List<ClinicalData> _outliers = new ArrayList<>();
            List<ClinicalData> _nonOutliers = new ArrayList<>();
            List<ClinicalData> detailedData = groupedDetailedData.get(category);
            int numSuspectedOutliers = 0;
            for (ClinicalData d: detailedData) {
                Double value = useLogScale ? ViolinPlotServiceImpl.logScale(Double.parseDouble(d.getAttrValue())) : Double.parseDouble(d.getAttrValue());
                boolean isOutlier = false;
                if (value <= suspectedOutlierThresholdLower) {
                    numSuspectedOutliers += 1;
                    if (value <= outlierThresholdLower) {
                        isOutlier = true;
                    }
                } else if (value >= suspectedOutlierThresholdUpper) {
                    numSuspectedOutliers += 1;
                    if (value >= outlierThresholdUpper) {
                        isOutlier = true;
                    }
                }
                if (isOutlier) {
                    _outliers.add(d);
                } else {
                    _nonOutliers.add(d);
                }
            }

            ClinicalViolinPlotBoxData _boxData = new ClinicalViolinPlotBoxData();
            _boxData.setMedian(percentile.evaluate(50));
            _boxData.setQ1(q1);
            _boxData.setQ3(q3);
            _boxData.setWhiskerLower(numSuspectedOutliers > 0 ? suspectedOutlierThresholdLower : min);
            _boxData.setWhiskerUpper(numSuspectedOutliers > 0 ? suspectedOutlierThresholdUpper : max);

            result.setAxisStart(Math.min(result.getAxisStart(), min));
            result.setAxisEnd(Math.max(result.getAxisEnd(), max));

            nonOutliers.put(category, _nonOutliers);
            outliers.put(category, _outliers);
            boxData.put(category, _boxData);
        });

        // Set axis bounds from parameters, if given
        if (axisStart != null) {
            result.setAxisStart(axisStart.doubleValue());
        }
        if (axisEnd != null) {
            result.setAxisEnd(axisEnd.doubleValue());
        }

        // Create curves
        // By this point, we know the axis bounds
        List<Double> curvePoints = new ArrayList<>();
        Double stepSize = (result.getAxisEnd() - result.getAxisStart()) / (numCurvePoints.doubleValue()-1);
        for (int i=0; i<numCurvePoints.intValue(); i++) {
            curvePoints.add(result.getAxisStart() + i*stepSize);
        }
        double sigma = sigmaMultiplier.doubleValue()*stepSize;
        List<ClinicalViolinPlotRowData> rows = result.getRows();
        nonOutliers.forEach((category, data)->{
            ClinicalViolinPlotRowData row = new ClinicalViolinPlotRowData();
            row.setCategory(category);
            row.setNumSamples(data.size() + outliers.get(category).size());
            row.setBoxData(boxData.get(category).limitWhiskers(result));

            List<ClinicalData> _individualPoints = new ArrayList<>();

            if (row.getNumSamples() <= SHOW_ONLY_POINTS_THRESHOLD) {
                // show only individual points when data is small
                row.setCurveData(new ArrayList<>());
                _individualPoints.addAll(data);
                _individualPoints.addAll(outliers.get(category));
            } else {
                // build violin only based on non-outliers
                List<Gaussian> gaussians = new ArrayList<>();
                for (ClinicalData d : data) {
                    Double value = useLogScale ? ViolinPlotServiceImpl.logScale(Double.parseDouble(d.getAttrValue())) : Double.parseDouble(d.getAttrValue());
                    gaussians.add(new Gaussian(value, sigma));
                }

                row.setCurveData(
                    curvePoints.stream().map(p -> {
                        BigDecimal sum = new BigDecimal(0);
                        for (Gaussian g : gaussians) {
                            sum = sum.add(BigDecimal.valueOf(g.value(p)));
                        }
                        return sum.doubleValue();
                    }).collect(Collectors.toList())
                );

                // render outliers as individual points
                _individualPoints = outliers.get(category);
            }

            List<ClinicalViolinPlotIndividualPoint> individualPoints = new ArrayList<>();
            for (ClinicalData d: _individualPoints) {
                ClinicalViolinPlotIndividualPoint p = new ClinicalViolinPlotIndividualPoint();
                p.setSampleId(d.getSampleId());
                p.setStudyId(d.getStudyId());
                p.setValue(useLogScale ? ViolinPlotServiceImpl.logScale(Double.parseDouble(d.getAttrValue())) : Double.parseDouble(d.getAttrValue()));
                individualPoints.add(p);
            }
            row.setIndividualPoints(individualPoints);
            rows.add(row);
        });
        return result;
    }

    private static double logScale(double val) {
        return Math.log(1+val);
    }
}
