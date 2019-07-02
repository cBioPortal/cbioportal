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
        double start = 0;
        
        if (boxRange.lowerEndpoint() != 0) {
            double absLogValue = Math.log10(Math.abs(boxRange.lowerEndpoint()));
            start = boxRange.lowerEndpoint() < 0 ? -Math.ceil(absLogValue) : Math.floor(absLogValue);
        }
        
        if (lowerOutlier != null) {
            intervals.add(lowerOutlier);
        }
        
        for (double exponent = start; ; exponent += 0.5)
        {
            Double value = calcIntervalValue(exponent);
            
            if ((lowerOutlier == null || value > lowerOutlier) && 
                (upperOutlier == null || value <= upperOutlier)) {
                intervals.add(value);
            }

            if (value > boxRange.upperEndpoint())
            {
                value = calcIntervalValue(exponent + 0.5);
                
                if (upperOutlier == null || value <= upperOutlier) {
                    intervals.add(value);
                }
                else {
                    intervals.add(upperOutlier);
                }
                
                break;
            }
        }
        
        return dataBinHelper.initDataBins(attributeId, values, intervals);
    }
    
    public Double calcIntervalValue(Double exponent)
    {
        return Math.signum(exponent) * Math.floor(Math.pow(10, Math.abs(exponent)));
    }
}
