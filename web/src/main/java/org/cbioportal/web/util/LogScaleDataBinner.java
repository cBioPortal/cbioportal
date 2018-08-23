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

        List<Double> filteredIntervals = dataBinHelper.filterIntervals(intervals, lowerOutlier, upperOutlier);

        // we don't want intervals start or end with non-integer powers of 10, so adjust if needed
        if (filteredIntervals.size() > 0)
        {
            Double first = filteredIntervals.get(0);
            Double last = filteredIntervals.get(filteredIntervals.size() - 1);
            
            if (first != 1 && first % 10 != 0) {
                first = Math.pow(10, Math.floor(Math.log10(first)));
                
                if (!lowerOutlier.equals(boxRange.lowerEndpoint()) && first < lowerOutlier) {
                    filteredIntervals.remove(0);
                }
                else {
                    filteredIntervals.add(0, first);
                }
            }
            
            if (last != 1 && last % 10 != 0) {
                last = Math.pow(10, Math.ceil(Math.log10(last)));
                
                if (!upperOutlier.equals(boxRange.upperEndpoint()) && last > upperOutlier) {
                    filteredIntervals.remove(filteredIntervals.size() - 1);
                }
                else {
                    filteredIntervals.add(last);
                }
            }
        }
        
        return dataBinHelper.initDataBins(attributeId, values, filteredIntervals);
    }
}
