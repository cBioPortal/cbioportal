package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.summary.FilterParams;
import org.cbioportal.model.summary.Range;
import org.cbioportal.persistence.summary.SummaryApi;
import org.cbioportal.persistence.summary.SummaryApiImpl;
import org.cbioportal.persistence.summary.SummaryServer;
import org.cbioportal.persistence.summary.SummaryServerConfig;
import org.cbioportal.service.EnclaveApiService;
import org.cbioportal.service.SummaryDataService;
import org.cbioportal.web.parameter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class SummaryDataServiceImpl implements SummaryDataService {
    
    @Autowired
    private SummaryServerConfig summaryServerConfig;
    
    @Override
    public boolean supportsStudies(List<String> studyIds) {
        return studyIds.size() == 1 && studyIds.get(0).equals("enclave_2024");
    }

    @Override
    public List<ClinicalAttribute> fetchClinicalAttributes(List<String> studyIds, Projection projection) {
        SummaryApi summaryApi = new SummaryApiImpl(summaryServerConfig.getServers().get(0));
        return summaryApi.fetchClinicalAttributes(studyIds, projection).get();
    }
    
    @Override
    public List<ClinicalDataCountItem> fetchClinicalDataCounts(
        ClinicalDataCountFilter filter
    ) {
        SummaryApi summaryApi = new SummaryApiImpl(summaryServerConfig.getServers().get(0));
        return summaryApi.fetchClinicalDataCounts(filter).get();
    }

    @Override
    public List<ClinicalDataBin> fetchClinicalDataBinCounts(
        ClinicalDataBinCountFilter filter,
        DataBinMethod dataBinMethod
    ) {
        SummaryApi summaryApi = new SummaryApiImpl(summaryServerConfig.getServers().get(0));
        return summaryApi.fetchClinicalDataBinCounts(filter, dataBinMethod).get();
    }
}
