package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
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
        return clinicalAttributeService.fetchClinicalAttributes(this.visibleStudies, "SUMMARY");
    }

    @Override
    public List<ClinicalDataCountItem> fetchClinicalDataCounts(
        ClinicalDataCountFilter filter
    ) {
        // TODO: replicate the logic for cacheableClinicalDataCounts here
        filter.getStudyViewFilter().setStudyIds(visibleStudies);
        return cachedClinicalDataCounts(filter);
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
    
    private List<String> filterStudyIds(List<String> requestedStudyIds) {
        var result = new ArrayList<String>();
        for (String studyId : requestedStudyIds) {
            if (visibleStudies.contains(studyId)) {
                result.add(studyId);
            }
        }
        return requestedStudyIds;
    }
}