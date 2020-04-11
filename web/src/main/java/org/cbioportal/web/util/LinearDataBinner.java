package org.cbioportal.web.util;

import com.google.common.collect.Range;
import org.cbioportal.model.DataBin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LinearDataBinner {
    public static final Double[] POSSIBLE_INTERVALS = {
        0.001, 0.002, 0.0025, 0.005, 0.01,
        0.02, 0.025, 0.05, 0.1,
        0.2, 0.25, 0.5, 1.0,
        2.0, 5.0, 10.0,
        20.0, 25.0, 50.0, 100.0,
        200.0, 250.0, 500.0, 1000.0,
        2000.0, 2500.0, 5000.0, 10000.0
    };
    
    public static final Double[] POSSIBLE_DISCRETE_INTERVALS = {
            1.0, 2.0, 5.0, 10.0,
            20.0, 25.0, 50.0, 100.0,
            200.0, 250.0, 500.0, 1000.0,
            2000.0, 2500.0, 5000.0, 10000.0
    };

    public static final Integer DEFAULT_INTERVAL_COUNT = 20;

    private DataBinHelper dataBinHelper;

    @Autowired
    public LinearDataBinner(DataBinHelper dataBinHelper) {
        this.dataBinHelper = dataBinHelper;
    }

    public List<DataBin> calculateDataBins(boolean areAllIntegers,
                                           Range<BigDecimal> boxRange,
                                           List<BigDecimal> values,
                                           BigDecimal lowerOutlier,
                                           BigDecimal upperOutlier,
                                           Optional<String> attributeId) {
        BigDecimal min = lowerOutlier == null ? Collections.min(values) : Collections.min(values).max(lowerOutlier);
        BigDecimal max = upperOutlier == null ? Collections.max(values) : Collections.max(values).min(upperOutlier);

        List<DataBin> dataBins = initDataBins(areAllIntegers, min, max, lowerOutlier, upperOutlier);

        // special case for "AGE" attributes
        if (attributeId.isPresent() &&
            dataBinHelper.isAgeAttribute(attributeId.get()) &&
            min.doubleValue() < 18 &&
            boxRange.upperEndpoint().subtract(boxRange.lowerEndpoint()).divide(BigDecimal.valueOf(2)).compareTo(BigDecimal.valueOf(18)) == 1 &&
            dataBins.get(0).getEnd().compareTo(BigDecimal.valueOf(18)) == 1) {
            // force first bin to start from 18
            dataBins.get(0).setStart(BigDecimal.valueOf(18));
        }

        dataBinHelper.calcCounts(dataBins, values);

        return dataBins;
    }

    public List<DataBin> calculateDataBins(List<BigDecimal> customBins,
                                           List<BigDecimal> values) {
        List<DataBin> dataBins = initDataBins(customBins);
        dataBinHelper.calcCounts(dataBins, values);
        return dataBins;
    }

    public List<DataBin> initDataBins(List<BigDecimal> bins) {
        List<DataBin> dataBins = new ArrayList<>();
        for (int i = 0; i < bins.size() - 1; i++) {
            DataBin dataBin = new DataBin();
            dataBin.setStart(bins.get(i));
            dataBin.setEnd(bins.get(i + 1));
            dataBin.setCount(0);
            dataBins.add(dataBin);
        }
        return dataBins;
    }

    public List<DataBin> initDataBins(boolean areAllIntegers,
                                      BigDecimal min,
                                      BigDecimal max,
                                      BigDecimal lowerOutlier,
                                      BigDecimal upperOutlier) {
        List<DataBin> dataBins = new ArrayList<>();

        List<BigDecimal> possibleIntervals = Arrays
                .asList(areAllIntegers ? POSSIBLE_DISCRETE_INTERVALS : POSSIBLE_INTERVALS)
                .stream()
                .map(val -> BigDecimal.valueOf(val))
                .collect(Collectors.toList());

        BigDecimal interval = calcBinInterval(possibleIntervals, max.subtract(min), DEFAULT_INTERVAL_COUNT);

        BigDecimal start = min.add(interval).subtract(min.remainder(interval));

        // check lowerOutlier too for better tuning of start
        if (lowerOutlier == null || start.subtract(interval).compareTo(lowerOutlier) == 1) {
            start = start.subtract(interval);
        }

        // check upperOutlier too for better tuning of end
        BigDecimal end = upperOutlier == null || max.add(interval).compareTo(upperOutlier) == -1 ? max: max.subtract(interval);

        for (BigDecimal d = start; d.compareTo(end) != 1; ) {
            DataBin dataBin = new DataBin();
            BigDecimal newEnd = d.add(interval);

            dataBin.setStart(d);
            dataBin.setEnd(newEnd);
            dataBin.setCount(0);

            dataBins.add(dataBin);

            d = newEnd;
        }

        return dataBins;
    }

    public BigDecimal calcBinInterval(List<BigDecimal> possibleIntervals, BigDecimal totalRange, Integer maxIntervalCount) {
        BigDecimal interval = new BigDecimal("-1.0");

        for (int i = 0; i < possibleIntervals.size(); i++) {
            interval = possibleIntervals.get(i);
            BigDecimal count = totalRange.divide(interval);

            if (count.doubleValue() < maxIntervalCount) {
                break;
            }
        }

        return interval;
    }
}
