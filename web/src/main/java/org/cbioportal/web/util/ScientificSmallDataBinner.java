package org.cbioportal.web.util;

import com.google.common.collect.Range;
import org.cbioportal.model.DataBin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScientificSmallDataBinner
{
    private DataBinHelper dataBinHelper;

    @Autowired
    public ScientificSmallDataBinner(DataBinHelper dataBinHelper) {
        this.dataBinHelper = dataBinHelper;
    }

    public List<DataBin> calculateDataBins(String attributeId,
                                           List<Double> sortedNumericalValues,
                                           List<Double> valuesWithoutOutliers,
                                           Double lowerOutlier,
                                           Double upperOutlier)
    {
        List<Double> exponents = sortedNumericalValues
            .stream()
            .map(d -> dataBinHelper.calcExponent(d).doubleValue())
            .filter(d -> d != 0)
            .collect(Collectors.toList());

        Range<Double> exponentBoxRange = dataBinHelper.calcBoxRange(exponents);
        
        List<Double> intervals = new ArrayList<>();

        Double exponentRange = exponentBoxRange == null ? 
            null : exponentBoxRange.upperEndpoint() - exponentBoxRange.lowerEndpoint();
        
        if (exponentRange == null) {
            // data set is not compatible with the scientific small data binner,
            // just set one interval for the entire set
            intervals.add(sortedNumericalValues.get(0));
            intervals.add(sortedNumericalValues.get(sortedNumericalValues.size() - 1));
        }
        else if (exponentRange > 1)
        {
            Integer interval = Math.round(exponentRange.floatValue() / 4);

            for (int i = exponentBoxRange.lowerEndpoint().intValue() - interval;
                 i <= exponentBoxRange.upperEndpoint();
                 i += interval)
            {
                intervals.add(Math.pow(10, i));
            }
        }
        else if (exponentRange == 1)
        {
            intervals.add(Math.pow(10, exponentBoxRange.lowerEndpoint()) / 3);

            for (int i = exponentBoxRange.lowerEndpoint().intValue();
                 i <= exponentBoxRange.upperEndpoint().intValue() + 1;
                 i++)
            {
                intervals.add(Math.pow(10, i));
                intervals.add(3 * Math.pow(10, i));
            }
        }
        else // exponentRange == 0 
        {
            Double interval = 2 * Math.pow(10, exponentBoxRange.lowerEndpoint());

            for (double d = Math.pow(10, exponentBoxRange.lowerEndpoint());
                 d <= Math.pow(10, exponentBoxRange.upperEndpoint() + 1);
                 d += interval)
            {
                intervals.add(d);
            }
        }

        return dataBinHelper.initDataBins(
            attributeId, valuesWithoutOutliers, intervals, lowerOutlier, upperOutlier);
    }
}
