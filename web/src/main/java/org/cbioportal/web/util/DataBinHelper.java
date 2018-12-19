package org.cbioportal.web.util;

import com.google.common.collect.Range;
import org.cbioportal.model.DataBin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataBinHelper
{
    private StudyViewFilterUtil studyViewFilterUtil;

    @Autowired
    public DataBinHelper(StudyViewFilterUtil studyViewFilterUtil) {
        this.studyViewFilterUtil = studyViewFilterUtil;
    }

    public DataBin calcUpperOutlierBin(String attributeId, List<Double> gteValues, List<Double> gtValues)
    {
        Double gteMin = gteValues.size() > 0 ? Collections.min(gteValues) : null;
        Double gtMin = gtValues.size() > 0 ? Collections.min(gtValues) : null;
        Double min;
        String value;

        if (gtMin == null && gteMin == null) {
            // no special outlier
            min = null;
            value = ">";
        }
        else if (gtMin == null || (gteMin != null && gteMin < gtMin)) {
            min = gteMin;
            value = ">=";
        }
        else {
            min = gtMin;
            value = ">";
        }

        DataBin dataBin = new DataBin();

        dataBin.setAttributeId(attributeId);
        dataBin.setCount(gteValues.size() + gtValues.size());
        dataBin.setSpecialValue(value);
        dataBin.setStart(min);

        return dataBin;
    }

    public DataBin calcLowerOutlierBin(String attributeId, List<Double> lteValues, List<Double> ltValues)
    {
        Double lteMax = lteValues.size() > 0 ? Collections.max(lteValues) : null;
        Double ltMax = ltValues.size() > 0 ? Collections.max(ltValues) : null;
        Double max;
        String specialValue;

        if (ltMax == null && lteMax == null) {
            max = null;
            specialValue = "<=";
        }
        else if (lteMax == null || (ltMax != null && lteMax < ltMax)) {
            max = ltMax;
            specialValue = "<";
        }
        else {
            max = lteMax;
            specialValue = "<=";
        }

        DataBin dataBin = new DataBin();

        dataBin.setAttributeId(attributeId);
        dataBin.setCount(lteValues.size() + ltValues.size());
        dataBin.setSpecialValue(specialValue);
        dataBin.setEnd(max);

        return dataBin;
    }
    
    public Range<Double> calcBoxRange(List<Double> sortedValues)
    {
        if (sortedValues == null || sortedValues.size() == 0) {
            return null;
        }

        // Find a generous IQR. This is generous because if (values.length / 4) 
        // is not an int, then really you should average the two elements on either 
        // side to find q1 and q3.
        Range<Double> interquartileRange = calcInterquartileRange(sortedValues);

        Double q1 = interquartileRange.lowerEndpoint();
        Double q3 = interquartileRange.upperEndpoint();
        Double iqr = q3 - q1;

        // Then find min and max values
        Double maxValue;
        Double minValue;

        if (sortedValues.get(0).equals(sortedValues.get(sortedValues.size() - 1))) {
            // if the first and last values are the same, no need to do any other calculation
            // we simply set min and max to the same value
            minValue = sortedValues.get(0);
            maxValue = minValue;
        }
        else if (0.001 <= q3 && q3 < 1.0) {
            //maxValue = Number((q3 + iqr * 1.5).toFixed(3));
            //minValue = Number((q1 - iqr * 1.5).toFixed(3));
            maxValue = (new BigDecimal(q3 + iqr * 1.5)).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
            minValue = (new BigDecimal(q1 - iqr * 1.5)).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        } 
        else if (q3 < 0.001) {
            // get IQR for very small number(<0.001)
            maxValue = q3 + iqr * 1.5;
            minValue = q1 - iqr * 1.5;
        } 
        else {
            maxValue = Math.ceil(q3 + iqr * 1.5);
            minValue = Math.floor(q1 - iqr * 1.5);
        }
        
        if (minValue < sortedValues.get(0)) {
            minValue = sortedValues.get(0);
        }
        
        if (maxValue > sortedValues.get(sortedValues.size() - 1)) {
            maxValue = sortedValues.get(sortedValues.size() - 1);
        }

        return Range.closed(minValue, maxValue);
    }
    
    public Range<Double> calcInterquartileRange(List<Double> sortedValues)
    {
        Range<Double> iqr = null;
        
        if (sortedValues.size() > 0)
        {
            Double q1 = calcQ1(sortedValues);
            Double q3 = calcQ3(sortedValues);
            Double max = sortedValues.get(sortedValues.size() - 1);

            // if iqr == 0 AND max == q3 then recursively try finding a non-zero iqr
            if (q1.equals(q3) && max.equals(q3)) {
                // filter out max and try again
                iqr = this.calcInterquartileRange(
                    sortedValues.stream().filter(d -> d < max).collect(Collectors.toList()));
            }

            // if range is still empty use the original q1 and q3 values
            if (iqr == null || iqr.isEmpty()) {
                iqr = Range.closedOpen(q1, q3);
            }
        }
        
        return iqr;
    }
    
    public Double calcQ1(List<Double> sortedValues)
    {
        return sortedValues.size() > 0 ? 
            sortedValues.get((int) Math.floor(sortedValues.size() / 4.0)) : null;
    }

    public Double calcQ3(List<Double> sortedValues)
    {
        return sortedValues.size() > 0 ? 
            sortedValues.get((int) Math.floor(sortedValues.size() * (3.0 / 4.0))) : null;
    }
    
    public List<Double> filterIntervals(List<Double> intervals, Double lowerOutlier, Double upperOutlier)
    {
        // remove values that fall outside the lower and upper outlier limits
        return intervals.stream()
            .filter(d -> (lowerOutlier == null || d > lowerOutlier) && (upperOutlier == null || d < upperOutlier))
            .collect(Collectors.toList());
    }
    
    public List<DataBin> initDataBins(String attributeId,
                                      List<Double> values,
                                      List<Double> intervals,
                                      Double lowerOutlier,
                                      Double upperOutlier)
    {
        return initDataBins(attributeId,
            values,
            filterIntervals(intervals, lowerOutlier, upperOutlier));
    }

    public List<DataBin> initDataBins(String attributeId,
                                      List<Double> values,
                                      List<Double> intervals)
    {
        List<DataBin> dataBins = initDataBins(attributeId, intervals);

        calcCounts(dataBins, values);

        return dataBins;
    }
    
    public List<DataBin> initDataBins(String attributeId, List<Double> intervalValues)
    {
        List<DataBin> dataBins = new ArrayList<>();

        for (int i = 0; i < intervalValues.size() - 1; i++) {
            DataBin dataBin = new DataBin();

            dataBin.setAttributeId(attributeId);
            dataBin.setCount(0);
            dataBin.setStart(intervalValues.get(i));
            dataBin.setEnd(intervalValues.get(i+1));

            dataBins.add(dataBin);
        }

        return dataBins;
    }

    public List<DataBin> trim(List<DataBin> dataBins)
    {
        List<DataBin> toRemove = new ArrayList<>();
        
        // find out leading empty bins
        for (DataBin dataBin : dataBins)
        {
            if (dataBin.getCount() == null || dataBin.getCount() <= 0) {
                toRemove.add(dataBin);
            }
            else {
                break;
            }
        }
        
        // find out trailing empty bins
        ListIterator<DataBin> iterator = dataBins.listIterator(dataBins.size());
        
        while (iterator.hasPrevious()) {
            DataBin dataBin = iterator.previous();

            if (dataBin.getCount() == null || dataBin.getCount() <= 0) {
                toRemove.add(dataBin);
            }
            else {
                break;
            }
        }
        
        List<DataBin> trimmed = new ArrayList<>(dataBins);
        trimmed.removeAll(toRemove);
        
        return trimmed;
    }
    
    public void calcCounts(List<DataBin> dataBins, List<Double> values)
    {
        Map<Range<Double>, DataBin> rangeMap = dataBins.stream().collect(Collectors.toMap(this::calcRange, b -> b));
        
        // TODO complexity here is O(n x m), find a better way to do this
        for (Range<Double> range : rangeMap.keySet()) {
            for (Double value: values) {
                // check if the value falls within the data bin range
                if (range != null && range.contains(value)) {
                    DataBin dataBin = rangeMap.get(range); 
                    dataBin.setCount(dataBin.getCount() + 1);
                }
            }
        }
    }
    
    public Range<Double> calcRange(DataBin dataBin)
    {
        boolean startInclusive = ">=".equals(dataBin.getSpecialValue());
        boolean endInclusive = !"<".equals(dataBin.getSpecialValue());
        
        // special condition (start == end)
        if (dataBin.getStart() != null && dataBin.getStart().equals(dataBin.getEnd())) {
            startInclusive = endInclusive = true;
        }
        
        return studyViewFilterUtil.calcRange(dataBin.getStart(), startInclusive, dataBin.getEnd(), endInclusive);
    }

    public Range<Double> calcRange(String operator, Double value)
    {
        boolean startInclusive = ">=".equals(operator);
        Double start = operator.contains(">") ? value : null;
        boolean endInclusive = !"<".equals(operator);
        Double end = operator.contains("<") ? value : null;

        return studyViewFilterUtil.calcRange(start, startInclusive, end, endInclusive);
    }

    public boolean isNA(String value)
    {
        return value.toUpperCase().equals("NA") || 
            value.toUpperCase().equals("NAN") || 
            value.toUpperCase().equals("N/A");
    }
    
    public boolean isSmallData(List<Double> sortedValues)
    {
        Double median = sortedValues.get((int) Math.ceil((sortedValues.size() * (1.0 / 2.0))));
        
        return 0.001 > median && median > -0.001 && !median.equals(0.0);
    }

    public String extractOperator(String value)
    {
        int length = 0;

        if (value.trim().startsWith(">=") || value.trim().startsWith("<=")) {
            length = 2;
        }
        else if (value.trim().startsWith(">") || value.trim().startsWith("<")) {
            length = 1;
        }

        return value.trim().substring(0, length);
    }
    
    public Integer calcExponent(Double value)
    {
        BigDecimal decimal = new BigDecimal(value);

        return decimal.precision() - decimal.scale() - 1;
    }

    public String stripOperator(String value) 
    {
        int length = 0;

        if (value.trim().startsWith(">=") || value.trim().startsWith("<=")) {
            length = 2;
        }
        else if (value.trim().startsWith(">") || value.trim().startsWith("<")) {
            length = 1;
        }

        return value.trim().substring(length);
    }

    public boolean isAgeAttribute(String attributeId)
    {
        return attributeId != null && attributeId.matches("(^AGE$)|(^AGE_.*)|(.*_AGE_.*)|(.*_AGE&)");
    }
}
