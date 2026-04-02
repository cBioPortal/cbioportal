package org.cbioportal.legacy.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalViolinPlotBoxData;
import org.cbioportal.legacy.model.ClinicalViolinPlotData;
import org.cbioportal.legacy.model.ClinicalViolinPlotIndividualPoint;
import org.cbioportal.legacy.model.ClinicalViolinPlotRowData;
import org.cbioportal.legacy.service.ViolinPlotService;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ViolinPlotServiceImpl implements ViolinPlotService {
  // If a row has less than this many points, do not compute a
  //  violin, because it doesn't make sense.
  static final int SHOW_ONLY_POINTS_THRESHOLD = 7;
  // For large datasets, cap the total number of points used for violin computation.
  static final int LARGE_DATASET_TOTAL_POINTS_THRESHOLD = 20000;
  static final int LARGE_DATASET_TOTAL_SAMPLE_MAX = 5000;
  // Reduce curve resolution for large plots: 50 points cuts Gaussian work by 50% vs 100.
  static final int LARGE_DATASET_CURVE_POINTS = 50;
  // Keep individual points payload bounded to avoid browser overload.
  static final int MAX_INDIVIDUAL_POINTS_PER_ROW = 100;
  static final int MAX_TOTAL_INDIVIDUAL_POINTS = 5000;

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition =
          "@cacheEnabledConfig.getEnabledClickhouse() && @studyViewFilterUtil.isUnfilteredQuery(#studyViewFilter)")
  public ClinicalViolinPlotData getClinicalViolinPlotData(
      List<ClinicalData> sampleClinicalDataForViolinPlot,
      Set<Integer> samplesForSampleCountsIds,
      BigDecimal axisStart,
      BigDecimal axisEnd,
      BigDecimal numCurvePoints,
      Boolean useLogScale,
      BigDecimal sigmaMultiplier,
      StudyViewFilter studyViewFilter) {
    ClinicalViolinPlotData result = new ClinicalViolinPlotData();
    result.setAxisStart(Double.POSITIVE_INFINITY);
    result.setAxisEnd(Double.NEGATIVE_INFINITY);
    result.setRows(new ArrayList<>());

    // clinicalDataMap is a map sampleId->studyId->data
    Map<String, Map<String, List<ClinicalData>>> clinicalDataMap =
        sampleClinicalDataForViolinPlot.stream()
            .collect(
                Collectors.groupingBy(
                    ClinicalData::getSampleId, Collectors.groupingBy(ClinicalData::getStudyId)));

    // Group data by category
    Map<String, List<ClinicalData>> groupedDetailedData = new HashMap<>();
    clinicalDataMap.forEach(
        (studyId, dataBySampleId) ->
            dataBySampleId.forEach(
                (sampleId, sampleData) -> {
                  // sampleData.size() == 2 means we have clinical data for the sample for both of
                  // the queried attributes
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

    // Preserve unsampled counts for display and filtering semantics.
    Map<String, Integer> trueNumSamplesByCategory = new HashMap<>();
    groupedDetailedData.forEach(
        (category, data) ->
            trueNumSamplesByCategory.put(
                category, countFilteredSamples(samplesForSampleCountsIds, data)));

    // For large datasets, sample down per-category data before computation to reduce work.
    int totalDataPoints = groupedDetailedData.values().stream().mapToInt(List::size).sum();
    boolean isLargeDataset = totalDataPoints >= LARGE_DATASET_TOTAL_POINTS_THRESHOLD;
    if (isLargeDataset) {
      groupedDetailedData =
          sampleCategoriesForLargeDataset(groupedDetailedData, LARGE_DATASET_TOTAL_SAMPLE_MAX);
    }

    // Reduce curve resolution for large datasets.
    int effectiveCurvePoints =
        Math.max(
            2,
            isLargeDataset
                ? Math.min(LARGE_DATASET_CURVE_POINTS, numCurvePoints.intValue())
                : numCurvePoints.intValue());

    // Calculate boxes, outliers, and data bounds
    Map<String, ClinicalViolinPlotBoxData> boxData = new HashMap<>();
    Map<String, List<ClinicalData>> nonOutliers = new HashMap<>();
    Map<String, List<ClinicalData>> outliers = new HashMap<>();
    groupedDetailedData.forEach(
        (category, data) -> {
          Percentile percentile = new Percentile();
          // fill double[] to pass into Percentile
          double[] values = new double[data.size()];
          double min = Double.POSITIVE_INFINITY;
          double max = Double.NEGATIVE_INFINITY;
          int valuesIndex = 0;
          for (ClinicalData d : data) {
            if (NumberUtils.isCreatable(d.getAttrValue())) {
              Double value =
                  useLogScale
                      ? ViolinPlotServiceImpl.logScale(Double.parseDouble(d.getAttrValue()))
                      : Double.parseDouble(d.getAttrValue());
              values[valuesIndex] = value;
              min = Math.min(value, min);
              max = Math.max(value, max);
              valuesIndex += 1;
            }
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
          for (ClinicalData d : detailedData) {
            if (NumberUtils.isCreatable(d.getAttrValue())) {
              Double value =
                  useLogScale
                      ? ViolinPlotServiceImpl.logScale(Double.parseDouble(d.getAttrValue()))
                      : Double.parseDouble(d.getAttrValue());
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
    Double stepSize =
        (result.getAxisEnd() - result.getAxisStart()) / (effectiveCurvePoints - 1);
    for (int i = 0; i < effectiveCurvePoints; i++) {
      curvePoints.add(result.getAxisStart() + i * stepSize);
    }
    double sigma = sigmaMultiplier.doubleValue() * stepSize;
    List<ClinicalViolinPlotRowData> rows = result.getRows();
    final int[] remainingIndividualPointsBudget = new int[] {MAX_TOTAL_INDIVIDUAL_POINTS};
    nonOutliers.forEach(
        (category, data) -> {
          ClinicalViolinPlotRowData row = new ClinicalViolinPlotRowData();
          row.setCategory(category);
          row.setNumSamples(trueNumSamplesByCategory.getOrDefault(category, 0));
          row.setBoxData(boxData.get(category).limitWhiskers(result));

          List<ClinicalData> rowPointsToRender = new ArrayList<>();

          if (data.size() + outliers.get(category).size() <= SHOW_ONLY_POINTS_THRESHOLD) {
            // show only individual points when data is small
            row.setCurveData(new ArrayList<>());
            rowPointsToRender.addAll(data);
            rowPointsToRender.addAll(outliers.get(category));
          } else {
            // build violin only based on non-outliers
            List<Gaussian> gaussians = new ArrayList<>();
            for (ClinicalData d : data) {
              Double value =
                  useLogScale
                      ? ViolinPlotServiceImpl.logScale(Double.parseDouble(d.getAttrValue()))
                      : Double.parseDouble(d.getAttrValue());
              gaussians.add(new Gaussian(value, sigma));
            }

            row.setCurveData(
                curvePoints.parallelStream()
                    .map(
                        p -> {
                          BigDecimal sum = new BigDecimal(0);
                          for (Gaussian g : gaussians) {
                            sum = sum.add(BigDecimal.valueOf(g.value(p)));
                          }
                          return sum.doubleValue();
                        })
                    .collect(Collectors.toList()));

            // render outliers as individual points
            rowPointsToRender = outliers.get(category);
          }

          int allowedForRow =
              Math.min(MAX_INDIVIDUAL_POINTS_PER_ROW, remainingIndividualPointsBudget[0]);
          List<ClinicalData> individualPointsToRender = new ArrayList<>();
          if (allowedForRow > 0) {
            individualPointsToRender = sampleEvenly(rowPointsToRender, allowedForRow);
            remainingIndividualPointsBudget[0] -= individualPointsToRender.size();
          }

          List<ClinicalViolinPlotIndividualPoint> individualPoints = new ArrayList<>();
          for (ClinicalData d : individualPointsToRender) {
            ClinicalViolinPlotIndividualPoint p = new ClinicalViolinPlotIndividualPoint();
            p.setSampleId(d.getSampleId());
            p.setStudyId(d.getStudyId());
            p.setValue(
                useLogScale
                    ? ViolinPlotServiceImpl.logScale(Double.parseDouble(d.getAttrValue()))
                    : Double.parseDouble(d.getAttrValue()));
            individualPoints.add(p);
          }
          row.setIndividualPoints(individualPoints);
          rows.add(row);
        });

    // put everything into bins and then do one gaussian per bin, weighted by bin size
    return result;
  }

  @SafeVarargs
  private static int countFilteredSamples(
      Set<Integer> filteredSampleIds, List<ClinicalData>... dataLists) {
    return (int)
        Arrays.stream(dataLists)
            .flatMap(Collection::stream)
            .map(ClinicalData::getInternalId)
            .filter(filteredSampleIds::contains)
            .count();
  }

  private static double logScale(double val) {
    return Math.log(1 + val);
  }

  /**
   * For large datasets, sample each category proportionally to keep total computation bounded.
   */
  private static Map<String, List<ClinicalData>> sampleCategoriesForLargeDataset(
      Map<String, List<ClinicalData>> groupedData, int maxTotalSamples) {
    int totalDataPoints = groupedData.values().stream().mapToInt(List::size).sum();
    if (totalDataPoints <= maxTotalSamples) {
      return groupedData;
    }

    int categoryCount = groupedData.size();
    int effectiveMaxTotalSamples = Math.max(maxTotalSamples, categoryCount);
    double scale = (double) effectiveMaxTotalSamples / totalDataPoints;

    Map<String, Integer> samplesByCategory = new HashMap<>();
    Map<String, Double> remainderByCategory = new HashMap<>();
    int allocatedSamples = 0;

    for (Map.Entry<String, List<ClinicalData>> entry : groupedData.entrySet()) {
      int categorySize = entry.getValue().size();
      if (categorySize == 0) {
        samplesByCategory.put(entry.getKey(), 0);
        remainderByCategory.put(entry.getKey(), 0.0);
        continue;
      }

      double scaledSize = categorySize * scale;
      int allocated = Math.max(1, (int) Math.floor(scaledSize));
      allocated = Math.min(allocated, categorySize);

      samplesByCategory.put(entry.getKey(), allocated);
      remainderByCategory.put(entry.getKey(), scaledSize - Math.floor(scaledSize));
      allocatedSamples += allocated;
    }

    if (allocatedSamples < effectiveMaxTotalSamples) {
      List<String> categoriesByRemainderDesc =
          remainderByCategory.entrySet().stream()
              .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
              .map(Map.Entry::getKey)
              .collect(Collectors.toList());
      int budgetLeft = effectiveMaxTotalSamples - allocatedSamples;
      for (String category : categoriesByRemainderDesc) {
        if (budgetLeft == 0) {
          break;
        }
        int allocated = samplesByCategory.get(category);
        int categorySize = groupedData.get(category).size();
        if (allocated < categorySize) {
          samplesByCategory.put(category, allocated + 1);
          budgetLeft -= 1;
        }
      }
    }

    Map<String, List<ClinicalData>> sampled = new HashMap<>();
    for (Map.Entry<String, List<ClinicalData>> entry : groupedData.entrySet()) {
      sampled.put(
          entry.getKey(), sampleEvenly(entry.getValue(), samplesByCategory.get(entry.getKey())));
    }
    return sampled;
  }

  private static List<ClinicalData> sampleEvenly(List<ClinicalData> data, int maxSize) {
    if (data == null || maxSize <= 0) {
      return new ArrayList<>();
    }
    if (data.size() <= maxSize) {
      return data;
    }

    List<ClinicalData> sampled = new ArrayList<>(maxSize);
    double step = (double) data.size() / maxSize;
    for (int i = 0; i < maxSize; i++) {
      int index = Math.min((int) Math.floor(i * step), data.size() - 1);
      sampled.add(data.get(index));
    }
    return sampled;
  }
}
