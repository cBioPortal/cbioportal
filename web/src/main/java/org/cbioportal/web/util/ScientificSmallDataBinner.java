package org.cbioportal.web.util;

import com.google.common.collect.Range;
import org.cbioportal.model.DataBin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScientificSmallDataBinner {
    private DataBinHelper dataBinHelper;

    @Autowired
    public ScientificSmallDataBinner(DataBinHelper dataBinHelper) {
        this.dataBinHelper = dataBinHelper;
    }

    public List<DataBin> calculateDataBins(List<BigDecimal> sortedNumericalValues,
                                           List<BigDecimal> valuesWithoutOutliers,
                                           BigDecimal lowerOutlier,
                                           BigDecimal upperOutlier) {
        List<BigDecimal> exponents = sortedNumericalValues
            .stream()
            .map(d -> BigDecimal.valueOf(dataBinHelper.calcExponent(d)))
            .filter(d -> d.compareTo(new BigDecimal("0")) != 0)
            .collect(Collectors.toList());

        Range<BigDecimal> exponentBoxRange = dataBinHelper.calcBoxRange(exponents);

        List<BigDecimal> intervals = new ArrayList<>();

        BigDecimal exponentRange = exponentBoxRange == null ?
            null : exponentBoxRange.upperEndpoint().subtract(exponentBoxRange.lowerEndpoint());

        if (exponentRange == null) {
            // data set is not compatible with the scientific small data binner,
            // just set one interval for the entire set
            intervals.add(sortedNumericalValues.get(0));
            intervals.add(sortedNumericalValues.get(sortedNumericalValues.size() - 1));
        } else if (exponentRange.compareTo(new BigDecimal("1")) == 1) {
            Integer interval = Math.round(exponentRange.floatValue() / 4);

            for (int i = exponentBoxRange.lowerEndpoint().intValue() - interval;
                 i <= exponentBoxRange.upperEndpoint().intValue();
                 i += interval)
            {
                intervals.add(BigDecimal.valueOf(Math.pow(10, i)));
            }
        } else if (exponentRange.compareTo(new BigDecimal("1")) == 0) {
            intervals.add(BigDecimal.valueOf(Math.pow(10, exponentBoxRange.lowerEndpoint().doubleValue()) / 3));

            for (int i = exponentBoxRange.lowerEndpoint().intValue();
                 i <= exponentBoxRange.upperEndpoint().intValue() + 1;
                 i++)
            {
                BigDecimal powerTen = BigDecimal.valueOf(Math.pow(10, i));
                intervals.add(powerTen);
                intervals.add(powerTen.multiply(new BigDecimal("3")));
            }
        } else { // exponentRange == 0
            BigDecimal interval = BigDecimal.valueOf(2 * Math.pow(10, exponentBoxRange.lowerEndpoint().doubleValue()));

            for (BigDecimal d =  BigDecimal.valueOf(Math.pow(10, exponentBoxRange.lowerEndpoint().intValue()));
                 d.doubleValue() <= Math.pow(10, exponentBoxRange.upperEndpoint().doubleValue() + 1);
                 d=d.add(interval))
            {
                intervals.add(d);
            }
        }

        return dataBinHelper.initDataBins(valuesWithoutOutliers, intervals, lowerOutlier, upperOutlier);
    }
}
