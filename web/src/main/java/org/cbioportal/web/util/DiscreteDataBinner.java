package org.cbioportal.web.util;

import org.cbioportal.model.DataBin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DiscreteDataBinner {
    private DataBinHelper dataBinHelper;

    @Autowired
    public DiscreteDataBinner(DataBinHelper dataBinHelper) {
        this.dataBinHelper = dataBinHelper;
    }

    public List<DataBin> calculateDataBins(List<BigDecimal> values,
                                           Set<BigDecimal> uniqueValues) {
        List<DataBin> dataBins = initDataBins(uniqueValues);

        dataBinHelper.calcCounts(dataBins, values);

        return dataBins;
    }

    public List<DataBin> initDataBins(Set<BigDecimal> uniqueValues) {
        return uniqueValues.stream()
            .map(d -> {
                DataBin dataBin = new DataBin();

                dataBin.setCount(0);

                // set both start and end to the same value
                dataBin.setStart(d);
                dataBin.setEnd(d);

                return dataBin;
            })
            .collect(Collectors.toList());
    }
}
