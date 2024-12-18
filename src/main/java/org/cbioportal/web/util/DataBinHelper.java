package org.cbioportal.web.util;

import org.cbioportal.model.DataBin;
import com.google.common.collect.Range;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.util.Assert;

public class DataBinHelper {
   
    public static DataBin calcUpperOutlierBin(List<BigDecimal> gteValues, List<BigDecimal> gtValues) {
        BigDecimal gteMin = gteValues.size() > 0 ? Collections.min(gteValues) : null;
        BigDecimal gtMin = gtValues.size() > 0 ? Collections.min(gtValues) : null;
        BigDecimal min;
        String value;

        if (gtMin == null && gteMin == null) {
            // no special outlier
            min = null;
            value = ">";
        } else if (gtMin == null || (gteMin != null && gteMin.compareTo(gtMin) < 0)) {
            min = gteMin;
            value = ">=";
        } else {
            min = gtMin;
            value = ">";
        }

        DataBin dataBin = new DataBin();

        dataBin.setCount(gteValues.size() + gtValues.size());
        dataBin.setSpecialValue(value);
        dataBin.setStart(min);

        return dataBin;
    }

    public static DataBin calcLowerOutlierBin(List<BigDecimal> lteValues, List<BigDecimal> ltValues) {
        BigDecimal lteMax = lteValues.size() > 0 ? Collections.max(lteValues) : null;
        BigDecimal ltMax = ltValues.size() > 0 ? Collections.max(ltValues) : null;
        BigDecimal max;
        String specialValue;

        if (ltMax == null && lteMax == null) {
            max = null;
            specialValue = "<=";
        } else if (lteMax == null || (ltMax != null && lteMax.compareTo(ltMax) < 0)) {
            max = ltMax;
            specialValue = "<";
        } else {
            max = lteMax;
            specialValue = "<=";
        }

        DataBin dataBin = new DataBin();

        dataBin.setCount(lteValues.size() + ltValues.size());
        dataBin.setSpecialValue(specialValue);
        dataBin.setEnd(max);

        return dataBin;
    }
    
    public static List<BigDecimal> calcQuartileBoundaries(List<BigDecimal> sortedValues) {
        // Edge case: some of Q1, Q2, and Q3 are the same value.
        // Solution: reduce bins to represent unique values only.
        // Note: cannot use stream.distinct() because BigDecimal does
        // not play nice with this (e.g., "2.5E+2" is not identical to "250"
        // when using Object.equals())
        final BigDecimal q1 = calcQ1(sortedValues);
        final BigDecimal q2 = calcMedian(sortedValues);
        final BigDecimal q3 = calcQ3(sortedValues);
        List<BigDecimal> boundaries = new ArrayList<>();
        boundaries.add(q1);
        // Check Q1 smaller than Q2
        if (q1.compareTo(q2) < 0) {
            boundaries.add(q2);
        }
        // Check Q2 smaller than Q3
        if (q2.compareTo(q3) < 0) {
            boundaries.add(q3);
        }
        return boundaries;
    }

    public static Range<BigDecimal> calcBoxRange(List<BigDecimal> sortedValues) {
        if (sortedValues == null || sortedValues.size() == 0) {
            return null;
        }

        // Find a generous IQR. This is generous because if (values.length / 4)
        // is not an int, then really you should average the two elements on either
        // side to find q1 and q3.
        Range<BigDecimal> interquartileRange = calcInterquartileRangeApproximation(sortedValues);

        BigDecimal q1 = interquartileRange.lowerEndpoint();
        BigDecimal q3 = interquartileRange.upperEndpoint();
        BigDecimal iqr = q3.subtract(q1);
        BigDecimal iqrOneAndHalf = iqr.multiply(new BigDecimal("1.5"));
        BigDecimal q1LowerBoundry = q1.subtract(iqrOneAndHalf);
        BigDecimal q3upperBoundry = q3.add(iqrOneAndHalf);


        // Then find min and max values
        BigDecimal maxValue;
        BigDecimal minValue;

        if (sortedValues.get(0).compareTo(sortedValues.get(sortedValues.size() - 1)) == 0) {
            // if the first and last values are the same, no need to do any other calculation
            // we simply set min and max to the same value
            minValue = sortedValues.get(0);
            maxValue = minValue;
        } else if (q3.compareTo(new BigDecimal("0.001")) != -1 && q3.compareTo(new BigDecimal("1")) < 0) {
            //maxValue = Number((q3 + iqr * 1.5).toFixed(3));
            //minValue = Number((q1 - iqr * 1.5).toFixed(3));
            maxValue = q3upperBoundry.setScale(3, BigDecimal.ROUND_HALF_UP);
            minValue = q1LowerBoundry.setScale(3, BigDecimal.ROUND_HALF_UP);
        } else if (q3.compareTo(BigDecimal.valueOf(0.001)) < 0) {
            // get IQR for very small number(<0.001)
            maxValue = q3upperBoundry;
            minValue = q1LowerBoundry;
        } else {
            maxValue = q3upperBoundry.setScale(1, RoundingMode.CEILING);
            minValue = q1LowerBoundry.setScale(1, RoundingMode.FLOOR);
        }

        if (minValue.compareTo(sortedValues.get(0)) < 0) {
            minValue = sortedValues.get(0);
        }

        if (maxValue.compareTo(sortedValues.get(sortedValues.size() - 1)) > 0) {
            maxValue = sortedValues.get(sortedValues.size() - 1);
        }

        return Range.closed(minValue, maxValue);
    }

    public static Range<BigDecimal> calcInterquartileRangeApproximation(List<BigDecimal> sortedValues) {
        Range<BigDecimal> iqr = null;

        if (sortedValues.size() > 0) {
            BigDecimal q1 = valueCloseToQ1(sortedValues);
            BigDecimal q3 = valueCloseToQ3(sortedValues);
            BigDecimal max = sortedValues.get(sortedValues.size() - 1);

            // if iqr == 0 AND max == q3 then recursively try finding a non-zero iqr approximation.
            if (q1.compareTo(q3) == 0 && max.compareTo(q3) == 0) {
                // filter out max and try again
                iqr = calcInterquartileRangeApproximation(
                    sortedValues.stream().filter(d -> d.compareTo(max) < 0).collect(Collectors.toList()));
            }

            // if range is still empty use the original q1 and q3 values
            if (iqr == null || iqr.isEmpty()) {
                iqr = Range.closedOpen(q1, q3);
            }
        }

        return iqr;
    }

    public static BigDecimal calcQ1(List<BigDecimal> sortedValues) {
        if (sortedValues == null || sortedValues.isEmpty()) {
            return null;
        }
        // Stop is one position before the median array index.
        int stopIndex = (int) (sortedValues.size() * 0.5) - 1;
        return calcMedian(sortedValues, 0,  stopIndex);
    }

    public static BigDecimal calcMedian(List<BigDecimal> sortedValues) {
        return (sortedValues == null || sortedValues.isEmpty()) ? null
            : calcMedian(sortedValues, 0, sortedValues.size() - 1);
    }

    public static BigDecimal calcQ3(List<BigDecimal> sortedValues) {
        if (sortedValues == null || sortedValues.isEmpty()) {
            return null;
        }
        // Start one position after the median array index.
        int startIndex = (int) (sortedValues.size() * 0.5);
        if (sortedValues.size() % 2 != 0) {
            startIndex += 1;
        }
        return calcMedian(sortedValues, startIndex, sortedValues.size() - 1);
   }

    private static BigDecimal calcMedian(List<BigDecimal> sortedValues, int start, int stop) {
        if (sortedValues == null || sortedValues.isEmpty()) {
            return null;
        }
        final List<BigDecimal> values = sortedValues.subList(start, stop + 1);
        double index = values.size() * 0.5;
        BigDecimal value = values.get((int) index);
        if (values.size() % 2 != 0) {
            return value;
        } else {
            BigDecimal valueBelow = values.get((int) index - 1);
            return value.add(valueBelow).divide(new BigDecimal("2.0"));
        }
    }

    public static BigDecimal valueCloseToQ1(List<BigDecimal> sortedValues) {
        return (sortedValues == null || sortedValues.isEmpty()) ?
            null : sortedValues.get((int) (sortedValues.size() * 0.25));
    }

    public static BigDecimal valueCloseToQ3(List<BigDecimal> sortedValues) {
        return (sortedValues == null || sortedValues.isEmpty()) ?
            null : sortedValues.get((int) (sortedValues.size() * 0.75));
    }

    public static List<BigDecimal> filterIntervals(List<BigDecimal> intervals, BigDecimal lowerOutlier, BigDecimal upperOutlier) {
        // remove values that fall outside the lower and upper outlier limits
        return intervals.stream()
            .filter(d -> (lowerOutlier == null || d.compareTo(lowerOutlier) > 0 ) && (upperOutlier == null || d.compareTo(upperOutlier) < 0))
            .collect(Collectors.toList());
    }

    public static List<DataBin> initDataBins(List<BigDecimal> values,
                                      List<BigDecimal> intervals,
                                      BigDecimal lowerOutlier,
                                      BigDecimal upperOutlier) {
        return initDataBins(values,
            filterIntervals(intervals, lowerOutlier, upperOutlier));
    }

    public static List<DataBin> initDataBins(List<BigDecimal> values,
                                      List<BigDecimal> intervals) {
        List<DataBin> dataBins = initDataBins(intervals);

        calcCounts(dataBins, values);

        return dataBins;
    }

    public static List<DataBin> initDataBins(List<BigDecimal> intervalValues) {
        List<DataBin> dataBins = new ArrayList<>();

        for (int i = 0; i < intervalValues.size() - 1; i++) {
            DataBin dataBin = new DataBin();

            dataBin.setCount(0);
            dataBin.setStart(intervalValues.get(i));
            dataBin.setEnd(intervalValues.get(i+1));

            dataBins.add(dataBin);
        }

        return dataBins;
    }

    public static List<DataBin> trim(List<DataBin> dataBins) {
        List<DataBin> toRemove = new ArrayList<>();

        // find out leading empty bins
        for (DataBin dataBin : dataBins) {
            if (dataBin.getCount() == null || dataBin.getCount() <= 0) {
                toRemove.add(dataBin);
            } else {
                break;
            }
        }

        // find out trailing empty bins
        ListIterator<DataBin> iterator = dataBins.listIterator(dataBins.size());

        while (iterator.hasPrevious()) {
            DataBin dataBin = iterator.previous();

            if (dataBin.getCount() == null || dataBin.getCount() <= 0) {
                toRemove.add(dataBin);
            } else {
                break;
            }
        }

        List<DataBin> trimmed = new ArrayList<>(dataBins);
        trimmed.removeAll(toRemove);

        return trimmed;
    }

    public static void calcCounts(List<DataBin> dataBins, List<BigDecimal> values) {
        Map<Range<BigDecimal>, DataBin> rangeMap = dataBins.stream().collect(Collectors.toMap(DataBinHelper::calcRange, b -> b));

        // TODO complexity here is O(n x m), find a better way to do this
        for (Range<BigDecimal> range : rangeMap.keySet()) {
            for (BigDecimal value: values) {
                // check if the value falls within the data bin range
                if (range != null && range.contains(value)) {
                    DataBin dataBin = rangeMap.get(range);
                    dataBin.setCount(dataBin.getCount() + 1);
                }
            }
        }
    }

    public static Range<BigDecimal> calcRange(DataBin dataBin) {
        boolean startInclusive = ">=".equals(dataBin.getSpecialValue());
        boolean endInclusive = !"<".equals(dataBin.getSpecialValue());

        // special condition (start == end)
        if (dataBin.getStart() != null && dataBin.getEnd() != null && dataBin.getStart().compareTo(dataBin.getEnd())==0) {
            startInclusive = endInclusive = true;
        }

        return calcRange(dataBin.getStart(), startInclusive, dataBin.getEnd(), endInclusive);
    }

    public static Range<BigDecimal> calcRange(String operator, BigDecimal value) {
        boolean startInclusive = ">=".equals(operator);
        BigDecimal start = operator.contains(">") ? value : null;
        boolean endInclusive = !"<".equals(operator);
        BigDecimal end = operator.contains("<") ? value : null;

        return calcRange(start, startInclusive, end, endInclusive);
    }

    public static boolean isNA(String value) {
        return value.equalsIgnoreCase("NA") ||
            value.equalsIgnoreCase("NAN") ||
            value.equalsIgnoreCase("N/A");
    }

    public static boolean isSmallData(List<BigDecimal> sortedValues) {
        BigDecimal median = sortedValues.get((int) Math.ceil((sortedValues.size() * (1.0 / 2.0))));

        return median.compareTo(new BigDecimal("0.001")) < 0 && median.compareTo(new BigDecimal("-0.001")) > 0 && median.compareTo(new BigDecimal("0")) != 0;
    }

    public static String extractOperator(String value) {
        int length = 0;

        if (value.trim().startsWith(">=") || value.trim().startsWith("<=")) {
            length = 2;
        } else if (value.trim().startsWith(">") || value.trim().startsWith("<")) {
            length = 1;
        }

        return value.trim().substring(0, length);
    }

    public static Integer calcExponent(BigDecimal value) {
        return value.precision() - value.scale() - 1;
    }

    public static String stripOperator(String value) {
        int length = 0;

        if (value.trim().startsWith(">=") || value.trim().startsWith("<=")) {
            length = 2;
        } else if (value.trim().startsWith(">") || value.trim().startsWith("<")) {
            length = 1;
        }

        return value.trim().substring(length);
    }

    public static boolean isAgeAttribute(String attributeId) {
        return attributeId != null && attributeId.matches("(^AGE$)|(^AGE_.*)|(.*_AGE_.*)|(.*_AGE&)");
    }

    public static Range<BigDecimal> calcRange(BigDecimal start, boolean startInclusive, BigDecimal end, boolean endInclusive) {
        // check for invalid filter (no start or end provided)
        if (start == null && end == null) {
            return null;
        } else if (start == null) {
            if (endInclusive) {
                return Range.atMost(end);
            } else {
                return Range.lessThan(end);
            }
        } else if (end == null) {
            if (startInclusive) {
                return Range.atLeast(start);
            } else {
                return Range.greaterThan(start);
            }
        } else if (startInclusive) {
            if (endInclusive) {
                return Range.closed(start, end);
            } else {
                return Range.closedOpen(start, end);
            }
        } else {
            if (endInclusive) {
                return Range.openClosed(start, end);
            } else {
                return Range.open(start, end);
            }
        }
    }

    public static Set<BigDecimal> findDistinctValues(DataBin numericalBin, List<BigDecimal> numericalValues) {
        Range<BigDecimal> range = calcRange(numericalBin);
        
        return numericalValues.stream().filter(range::contains).collect(Collectors.toSet());
    }

    public static Set<Range<BigDecimal>> findDistinctSpecialRanges(DataBin numericalBin, List<Range<BigDecimal>> rangeValues) {
        Range<BigDecimal> range = calcRange(numericalBin);
        
        return rangeValues.stream().filter(range::encloses).collect(Collectors.toSet());
    }
    
    public static List<DataBin> convertToDistinctBins(
        List<DataBin> dataBins,
        List<BigDecimal> numericalValues,
        List<Range<BigDecimal>> rangeValues
    ) {
        List<DataBin> distinctBins = new ArrayList<>();
        
        for (DataBin bin: dataBins) {
            Set<BigDecimal> distinctValues = findDistinctValues(bin, numericalValues);
            Set<Range<BigDecimal>> distinctRanges = findDistinctSpecialRanges(bin, rangeValues);
            
            // if the bin contains only one distinct value and no range value then create a distinct bin
            if (distinctRanges.size() == 0 && distinctValues.size() == 1 && areAllIntegers(distinctValues)) {
                BigDecimal distinctValue = distinctValues.iterator().next();
                
                DataBin distinctBin = new DataBin();
                distinctBin.setCount(bin.getCount());
                distinctBin.setStart(distinctValue);
                distinctBin.setEnd(distinctValue);
                
                distinctBins.add(distinctBin);
            }
            // else keep the bin as is
            else {
                distinctBins.add(bin);
            }
        }
        
        // all bins except the outlier bins has to be distinct,
        // otherwise return the original input bins (no conversion)
        if (areAllDistinctExceptOutliers(distinctBins)) {
            return distinctBins;
        }
        else {
            return dataBins;
        }
    }
    
    public static Boolean areAllDistinctExceptOutliers(List<DataBin> dataBins) {
        return dataBins
            .stream()
            .filter(b -> b.getStart() != null && b.getEnd() != null)
            .map(b -> b.getStart().equals(b.getEnd()))
            .reduce((a, b) -> a && b)
            .orElse(false);
    }
    
    public static Boolean areAllIntegers(Set<BigDecimal> uniqueValues) {
        return uniqueValues
            .stream()
            .map(value -> value.stripTrailingZeros().scale() <= 0)
            .reduce((a, b) -> a && b)
            .orElse(false);
    }

    public static  List<BigDecimal> generateBins(List<BigDecimal> sortedNumericalValues, BigDecimal binSize, BigDecimal anchorValue) {

        Assert.notNull(sortedNumericalValues, "sortedNumerical values is null!");
        Assert.notNull(binSize, "binSize values is null!");
        Assert.notNull(anchorValue, "anchorValue values is null!");
        
        if (sortedNumericalValues.isEmpty()) {
            return null;
        }

        // Assumes that elements are sorted in ascending order
        BigDecimal minValue = sortedNumericalValues.get(0);
        BigDecimal maxValue = sortedNumericalValues.get(sortedNumericalValues.size()-1);
        Assert.isTrue(minValue.compareTo(maxValue) < 1, "minValue larger than maxValue. Input is not sorted in ascending order!");
        
        List<BigDecimal> bins = new ArrayList<>();
        
        // Calculate the lower boundary.
        BigDecimal deltaL = anchorValue.subtract(minValue);
        BigDecimal remainderL = deltaL.remainder(binSize);
        // remainder() is not modulo; correct for this. 
        if (remainderL.compareTo(new BigDecimal(0)) < 0) {
            remainderL = remainderL.add(binSize);
        }
        BigDecimal lowerBound = minValue.add(remainderL);
        
        // While the bound smaller than the maxValue keep adding boundaries.
        while (lowerBound.compareTo(maxValue) < 0) {
            bins.add(lowerBound);
            lowerBound = lowerBound.add(binSize);
        }
        
        return bins;
    }
    
    private BigDecimal min(List<BigDecimal> numericalValues) {
        return numericalValues.size() > 0 ? Collections.min(numericalValues) : null;
    }
    
    private BigDecimal max(List<BigDecimal> numericalValues) {
        return numericalValues.size() > 0 ? Collections.max(numericalValues) : null;
    }
}
