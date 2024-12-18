package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.persistence.fedapi.FederatedDataSource;
import org.cbioportal.persistence.fedapi.FederatedDataSourceImpl;
import org.cbioportal.persistence.fedapi.FederatedDataSourceConfig;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.FederatedViewService;
import org.cbioportal.service.exception.FederationException;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.ClinicalDataBinUtil;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

enum FederationMode {
    FEDERATOR,
    DATASOURCE,
    NONE
}

@Service
public class FederatedViewServiceImpl implements FederatedViewService {
    
    // TODO: why isn't this reading the value as expected?
    @Value("${fed.mode:NONE}")
    private FederationMode federationMode;
    
    @Autowired
    private FederatedDataSourceConfig federatedDataSourceConfig;
    
    @Autowired
    private ClinicalDataService clinicalDataService;
    
    @Autowired
    private ClinicalAttributeService clinicalAttributeService;
    
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;
    
    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;

    @Autowired
    private ClinicalDataBinUtil clinicalDataBinUtil;

    @Override
    public List<ClinicalAttribute> fetchClinicalAttributes() throws FederationException {
        if (federationMode == FederationMode.FEDERATOR) {
            try {
                FederatedDataSource federatedDataSource = new FederatedDataSourceImpl(federatedDataSourceConfig.getSources().get(0));
                return federatedDataSource.fetchClinicalAttributes(studyIds, projection).get();
            } catch (Exception e) {
                throw new FederationException("Failed to fetch clinical attributes", e);
            }
        } else if (federationMode == FederationMode.DATASOURCE) {
            return clinicalAttributeService.fetchClinicalAttributes(studyIds, projection);
        } else {
            throw new FederationException("Federation is disabled");
        }
    }
    
    @Override
    public List<ClinicalDataCountItem> fetchClinicalDataCounts(
        ClinicalDataCountFilter filter
    ) throws FederationException {
        if (federationMode == FederationMode.FEDERATOR) {
            try {
                FederatedDataSource federatedDataSource = new FederatedDataSourceImpl(federatedDataSourceConfig.getSources().get(0));
                return federatedDataSource.fetchClinicalDataCounts(filter).get();
            } catch (Exception e) {
                throw new FederationException("Failed to fetch clinical data counts", e);
            }
        } else if (federationMode == FederationMode.DATASOURCE) {
            // TODO: replicate the logic for cacheableClinicalDataCounts here
            return cachedClinicalDataCounts(filter);
        } else {
            throw new FederationException("Federation is disabled");
        }
    }

    public List<ClinicalDataCountItem> cachedClinicalDataCounts(ClinicalDataCountFilter interceptedClinicalDataCountFilter) {
        List<ClinicalDataFilter> attributes = interceptedClinicalDataCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = interceptedClinicalDataCountFilter.getStudyViewFilter();
        if (attributes.size() == 1) {
            studyViewFilterUtil.removeSelfFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
        }
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewFilterApplier.apply(studyViewFilter);

        if (filteredSampleIdentifiers.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(filteredSampleIdentifiers, studyIds, sampleIds);

        List<ClinicalDataCountItem> result = clinicalDataService.fetchClinicalDataCounts(
            studyIds, sampleIds, attributes.stream().map(a -> a.getAttributeId()).collect(Collectors.toList()));

        return result;
    }

    @Override
    public List<ClinicalDataBin> fetchClinicalDataBinCounts(
        ClinicalDataBinCountFilter filter
    ) throws FederationException {
        if (federationMode == FederationMode.FEDERATOR) {
            try {
                FederatedDataSource federatedDataSource = new FederatedDataSourceImpl(federatedDataSourceConfig.getSources().get(0));
                return federatedDataSource.fetchClinicalDataBinCounts(filter).get();
            } catch (Exception e) {
                throw new FederationException("Failed to fetch clinical data bin counts", e);
            }
        } else if (federationMode == FederationMode.DATASOURCE) {
            // TODO: replicate the logic for cacheableClinicalDataBinCounts here
            return cachedFetchClinicalDataBinCounts(filter);
        } else {
            throw new FederationException("Federation is disabled");
        }
    }

    public List<ClinicalDataBin> cachedFetchClinicalDataBinCounts(ClinicalDataBinCountFilter interceptedClinicalDataBinCountFilter) {
        return clinicalDataBinUtil.fetchClinicalDataBinCounts(
            DataBinMethod.STATIC,
            interceptedClinicalDataBinCountFilter,
            // we don't need to remove filter again since we already did it in the previous step 
            false
        );
    }
}
