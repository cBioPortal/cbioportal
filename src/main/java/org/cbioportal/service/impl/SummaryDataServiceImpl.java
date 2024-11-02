package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.persistence.summary.SummaryApi;
import org.cbioportal.persistence.summary.SummaryApiImpl;
import org.cbioportal.persistence.summary.SummaryApiConfig;
import org.cbioportal.service.SummaryDataService;
import org.cbioportal.service.exception.SummaryDataException;
import org.cbioportal.web.parameter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SummaryDataServiceImpl implements SummaryDataService {
    
    @Autowired
    private SummaryApiConfig summaryApiConfig;
    
    @Override
    public boolean supportsStudies(List<String> studyIds) {
        return studyIds.size() == 1 && studyIds.get(0).equals("enclave_2024");
    }

    @Override
    public List<ClinicalAttribute> fetchClinicalAttributes(
        List<String> studyIds,
        Projection projection
    ) throws SummaryDataException {
        try {
            SummaryApi summaryApi = new SummaryApiImpl(summaryApiConfig.getServers().get(0));
            return summaryApi.fetchClinicalAttributes(studyIds, projection).get();
        } catch (Exception e) {
            throw new SummaryDataException("Failed to fetch clinical attributes", e);
        }
    }
    
    @Override
    public List<ClinicalDataCountItem> fetchClinicalDataCounts(
        ClinicalDataCountFilter filter
    ) throws SummaryDataException {
        try {
            SummaryApi summaryApi = new SummaryApiImpl(summaryApiConfig.getServers().get(0));
            return summaryApi.fetchClinicalDataCounts(filter).get();
        } catch (Exception e) {
            throw new SummaryDataException("Failed to fetch clinical data counts", e);
        }
    }

    @Override
    public List<ClinicalDataBin> fetchClinicalDataBinCounts(
        ClinicalDataBinCountFilter filter,
        DataBinMethod dataBinMethod
    ) throws SummaryDataException {
        try {
            SummaryApi summaryApi = new SummaryApiImpl(summaryApiConfig.getServers().get(0));
            return summaryApi.fetchClinicalDataBinCounts(filter, dataBinMethod).get();
        } catch (Exception e) {
            throw new SummaryDataException("Failed to fetch clinical data bin counts", e);
        }
    }
}
