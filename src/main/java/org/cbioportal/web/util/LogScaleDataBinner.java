package org.cbioportal.web.util;

import com.google.common.collect.Range;
import org.cbioportal.model.DataBin;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class LogScaleDataBinner {

    public List<DataBin> calculateDataBins(Range<BigDecimal> boxRange,
                                           List<BigDecimal> values,
                                           BigDecimal lowerOutlier,
                                           BigDecimal upperOutlier) {
        List<BigDecimal> intervals = new ArrayList<>();
        BigDecimal start = new BigDecimal("0");

        if (boxRange.lowerEndpoint().compareTo(new BigDecimal("0")) != 0) {
            double absLogValue = Math.log10(boxRange.lowerEndpoint().abs().doubleValue());
            start = BigDecimal.valueOf(boxRange.lowerEndpoint().compareTo(new BigDecimal("0")) == -1 ? 
                -Math.ceil(absLogValue) : Math.floor(absLogValue));
        }

        if (lowerOutlier != null) {
            intervals.add(lowerOutlier);
        }

        for (BigDecimal exponent = start; ; exponent = exponent.add(new BigDecimal("0.5"))) {
            BigDecimal value = calcIntervalValue(exponent);

            if ((lowerOutlier == null || value.compareTo(lowerOutlier) == 1) &&
                (upperOutlier == null || value.compareTo(upperOutlier) != 1)) {
                intervals.add(value);
            }

            if (value.compareTo(boxRange.upperEndpoint()) == 1) {
                exponent = exponent.add(new BigDecimal("0.5"));

                if (!addOutlierInterval(intervals, exponent, upperOutlier)) {
                    intervals.add(upperOutlier);
                }
                
                // we don't want to end with non-integer exponent, 
                // add one more interval value if possible
                if (exponent.stripTrailingZeros().scale() > 0) {
                    exponent = exponent.add(new BigDecimal("0.5"));
                    addOutlierInterval(intervals, exponent, upperOutlier);
                }

                break;
            }
        }

        return DataBinHelper.initDataBins(values, intervals);
    }

    public BigDecimal calcIntervalValue(BigDecimal exponent) {
        return BigDecimal.valueOf(exponent.signum() * Math.floor(Math.pow(10, exponent.abs().doubleValue())));
    }
    
    private boolean addOutlierInterval(List<BigDecimal> intervals, 
                                       BigDecimal exponent, 
                                       BigDecimal upperOutlier)
    {
        BigDecimal value = calcIntervalValue(exponent);

        if (upperOutlier == null || value.compareTo(upperOutlier) != 1) {
            return intervals.add(value);
        }
        
        return false;
    }
}
