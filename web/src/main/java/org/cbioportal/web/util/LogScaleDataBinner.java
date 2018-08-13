package org.cbioportal.web.util;

import com.google.common.collect.Range;
import org.cbioportal.model.DataBin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LogScaleDataBinner 
{
    private DataBinHelper dataBinHelper;

    @Autowired
    public LogScaleDataBinner(DataBinHelper dataBinHelper) {
        this.dataBinHelper = dataBinHelper;
    }

    public List<DataBin> calculateDataBins(String attributeId,
                                           Range<Double> boxRange,
                                           List<Double> values,
                                           Double lowerOutlier,
                                           Double upperOutlier)
    {
        List<Double> intervals = new ArrayList<>();

        for (double d = 0; ; d += 0.5)
        {
            Double value = Math.floor(Math.pow(10, d));
            intervals.add(value);

            if (value > boxRange.upperEndpoint())
            {
                intervals.add(Math.pow(10, d + 0.5));
                break;
            }
        }

        return dataBinHelper.initDataBins(
            attributeId, values, intervals, lowerOutlier, upperOutlier);
    }
}
