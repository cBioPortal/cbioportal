package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.persistence.fedapi.FederatedDataSource;
import org.cbioportal.persistence.fedapi.FederatedDataSourceImpl;
import org.cbioportal.persistence.fedapi.FederatorConfig;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.FederatedService;
import org.cbioportal.service.exception.FederationException;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
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

@Service
@ConditionalOnProperty(name = "fedapi.mode", havingValue = "DATASOURCE")
public class FederatedDataSourceService implements FederatedService {
    
    @Value("${fedapi.datasource.display-name}")
    private String displayName;

    @Value("#{'${fedapi.datasource.visible-studies}'.split(',')}")
    private List<String> visibleStudies;

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
    public List<ClinicalAttribute> fetchClinicalAttributes() {
        var result = clinicalAttributeService.fetchClinicalAttributes(visibleStudies, "SUMMARY");
        return addDataSourceToClinicalAttributes(result);
    }
    
    private List<ClinicalAttribute> addDataSourceToClinicalAttributes(List<ClinicalAttribute> result) {
        for (String studyId : visibleStudies) {
            var dataSourceAttr = new ClinicalAttribute();
            dataSourceAttr.setAttrId("DATA_SOURCE");
            dataSourceAttr.setPatientAttribute(false);
            dataSourceAttr.setPriority("5000");
            dataSourceAttr.setCancerStudyIdentifier(studyId);
            dataSourceAttr.setDatatype("STRING");
            dataSourceAttr.setDescription("Name of the federated data source this sample originated from.");
            dataSourceAttr.setDisplayName("Data Source");
            
            result.add(dataSourceAttr);
        }
        return result;
    }

    @Override
    public List<ClinicalDataCountItem> fetchClinicalDataCounts(
        ClinicalDataCountFilter filter
    ) {
        // TODO: replicate the logic for cacheableClinicalDataCounts here
        filter.getStudyViewFilter().setStudyIds(visibleStudies);
        return cachedClinicalDataCounts(filter);
    }
    
    private List<ClinicalDataCountItem> addDataSourceToClinicalDataCounts(
        List<ClinicalDataCountItem> result,
        int sampleCount
    ) {
        var dataSourceCount = new ClinicalDataCount();
        dataSourceCount.setAttributeId("DATA_SOURCE");
        dataSourceCount.setValue(displayName);
        dataSourceCount.setCount(sampleCount);
        
        var dataSourceCountItem = new ClinicalDataCountItem();
        dataSourceCountItem.setAttributeId("DATA_SOURCE");
        dataSourceCountItem.setCounts(List.of(dataSourceCount));
        
        result.add(dataSourceCountItem);
        return result;
    }

    public List<ClinicalDataCountItem> cachedClinicalDataCounts(ClinicalDataCountFilter interceptedClinicalDataCountFilter) {
        
        List<ClinicalDataFilter> attributes = interceptedClinicalDataCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = interceptedClinicalDataCountFilter.getStudyViewFilter();

        // Remove DATA_SOURCE if it is present -- this is a virtual attribute we add ourselves, it is not stored in the db
        boolean dataSourceRequested = attributes.removeIf(attr -> attr.getAttributeId().equals("DATA_SOURCE"));
        
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

        if (dataSourceRequested) {
            result = addDataSourceToClinicalDataCounts(result, sampleIds.size());
        }
        
        return result;
    }

    @Override
    public List<ClinicalDataBin> fetchClinicalDataBinCounts(
        ClinicalDataBinCountFilter filter
    ) {
        // TODO: replicate the logic for cacheableClinicalDataBinCounts here
        filter.getStudyViewFilter().setStudyIds(visibleStudies);
        return cachedFetchClinicalDataBinCounts(filter);
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