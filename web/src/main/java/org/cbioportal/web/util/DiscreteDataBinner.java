package org.cbioportal.web.util;

import org.cbioportal.model.DataBin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DiscreteDataBinner
{
    private DataBinHelper dataBinHelper;

    @Autowired
    public DiscreteDataBinner(DataBinHelper dataBinHelper) {
        this.dataBinHelper = dataBinHelper;
    }

    public List<DataBin> calculateDataBins(String attributeId,
                                           List<Double> values,
                                           Set<Double> uniqueValues)
    {
        List<DataBin> dataBins = initDataBins(attributeId, uniqueValues);

        dataBinHelper.calcCounts(dataBins, values);

        return dataBins;
    }
    
    public List<DataBin> initDataBins(String attributeId,
                                      Set<Double> uniqueValues)
    {
        return uniqueValues.stream()
            .map(d -> {
                DataBin dataBin = new DataBin();

                dataBin.setAttributeId(attributeId);
                dataBin.setCount(0);

                // set both start and end to the same value
                dataBin.setStart(d);
                dataBin.setEnd(d);

                return dataBin;
            })
            .collect(Collectors.toList());
    }
}
