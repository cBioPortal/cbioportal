package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.persistence.fedapi.FederatedDataSource;
import org.cbioportal.persistence.fedapi.FederatedDataSourceImpl;
import org.cbioportal.persistence.fedapi.FederatedServerConfig;
import org.cbioportal.service.FederatedViewService;
import org.cbioportal.service.exception.FederationException;
import org.cbioportal.web.parameter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FederatedViewServiceImpl implements FederatedViewService {
    
    @Autowired
    private FederatedServerConfig federatedServerConfig;
    
    @Override
    public boolean supportsStudies(List<String> studyIds) {
        return studyIds.size() == 1 && studyIds.get(0).equals("enclave_2024");
    }

    @Override
    public List<ClinicalAttribute> fetchClinicalAttributes(
        List<String> studyIds,
        Projection projection
    ) throws FederationException {
        try {
            FederatedDataSource federatedDataSource = new FederatedDataSourceImpl(federatedServerConfig.getServers().get(0));
            return federatedDataSource.fetchClinicalAttributes(studyIds, projection).get();
        } catch (Exception e) {
            throw new FederationException("Failed to fetch clinical attributes", e);
        }
    }
    
    @Override
    public List<ClinicalDataCountItem> fetchClinicalDataCounts(
        ClinicalDataCountFilter filter
    ) throws FederationException {
        try {
            FederatedDataSource federatedDataSource = new FederatedDataSourceImpl(federatedServerConfig.getServers().get(0));
            return federatedDataSource.fetchClinicalDataCounts(filter).get();
        } catch (Exception e) {
            throw new FederationException("Failed to fetch clinical data counts", e);
        }
    }

    @Override
    public List<ClinicalDataBin> fetchClinicalDataBinCounts(
        ClinicalDataBinCountFilter filter,
        DataBinMethod dataBinMethod
    ) throws FederationException {
        try {
            FederatedDataSource federatedDataSource = new FederatedDataSourceImpl(federatedServerConfig.getServers().get(0));
            return federatedDataSource.fetchClinicalDataBinCounts(filter, dataBinMethod).get();
        } catch (Exception e) {
            throw new FederationException("Failed to fetch clinical data bin counts", e);
        }
    }
}
