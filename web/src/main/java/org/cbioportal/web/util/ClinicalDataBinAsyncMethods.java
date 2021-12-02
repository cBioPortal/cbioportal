package org.cbioportal.web.util;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.web.parameter.ClinicalDataBinFilter;
import org.cbioportal.web.parameter.ClinicalDataType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class ClinicalDataBinAsyncMethods {

    @Autowired
    private DataBinner dataBinner;

    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;

    @Async
    public CompletableFuture<List<ClinicalDataBin>> calculateStaticClinicalDataBins(ClinicalDataBinFilter attribute,
                                                                                    ClinicalDataType clinicalDataType,
                                                                                    List<ClinicalData> filteredClinicalData,
                                                                                    List<ClinicalData> unfilteredClinicalData,
                                                                                    List<String> filteredIds,
                                                                                    List<String> unfilteredIds) {
        List<ClinicalDataBin> dataBins = dataBinner
            .calculateClinicalDataBins(attribute, clinicalDataType,
                                       filteredClinicalData, unfilteredClinicalData,
                                       filteredIds, unfilteredIds)
            .stream()
            .map(dataBin -> studyViewFilterUtil.dataBinToClinicalDataBin(attribute, dataBin))
            .collect(Collectors.toList());

        return CompletableFuture.supplyAsync(() -> dataBins);
    }

    @Async
    public CompletableFuture<List<ClinicalDataBin>> calculateDynamicClinicalDataBins(ClinicalDataBinFilter attribute,
                                                                                     ClinicalDataType clinicalDataType,
                                                                                     List<ClinicalData> filteredClinicalData,
                                                                                     List<String> filteredIds) {
        List<ClinicalDataBin> dataBins = dataBinner
            .calculateDataBins(attribute, clinicalDataType,
                               filteredClinicalData,
                               filteredIds)
            .stream()
            .map(dataBin -> studyViewFilterUtil.dataBinToClinicalDataBin(attribute, dataBin))
            .collect(Collectors.toList());

        return CompletableFuture.supplyAsync(() -> dataBins);
    }
}
