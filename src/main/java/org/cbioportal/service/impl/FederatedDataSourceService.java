package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.FederatedService;
import org.cbioportal.service.PatientService;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.ClinicalDataBinUtil;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.opensaml.xmlsec.signature.P;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "fedapi.mode", havingValue = "DATASOURCE")
public class FederatedDataSourceService implements FederatedService {
    
    @Value("${fedapi.datasource.id}")
    private String dataSourceId;

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
    
    @Autowired
    private PatientService patientService;

    @Override
    public List<ClinicalAttribute> fetchClinicalAttributes() {
        var result = clinicalAttributeService.fetchClinicalAttributes(visibleStudies, "SUMMARY");
        return addDataSourceToClinicalAttributes(result);
    }
    
    private List<ClinicalAttribute> addDataSourceToClinicalAttributes(List<ClinicalAttribute> result) {
        for (String studyId : visibleStudies) {
            var dataSourceAttr = new ClinicalAttribute();
            dataSourceAttr.setAttrId("DATA_SOURCE");
            dataSourceAttr.setPatientAttribute(true);
            dataSourceAttr.setPriority("5000");
            dataSourceAttr.setCancerStudyIdentifier(studyId);
            dataSourceAttr.setDatatype("STRING");
            dataSourceAttr.setDescription("Name of the federated data source this patient belongs to.");
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
        int patientCount
    ) {
        var dataSourceCount = new ClinicalDataCount();
        dataSourceCount.setAttributeId("DATA_SOURCE");
        dataSourceCount.setValue(dataSourceId);
        dataSourceCount.setCount(patientCount);
        
        var dataSourceCountItem = new ClinicalDataCountItem();
        dataSourceCountItem.setAttributeId("DATA_SOURCE");
        dataSourceCountItem.setCounts(List.of(dataSourceCount));
        
        result.add(dataSourceCountItem);
        return result;
    }

    public List<ClinicalDataCountItem> cachedClinicalDataCounts(ClinicalDataCountFilter interceptedClinicalDataCountFilter) {
        
        List<ClinicalDataFilter> attributes = interceptedClinicalDataCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = interceptedClinicalDataCountFilter.getStudyViewFilter();
        
        if (!extractAndVerifyDataSourceFilter(studyViewFilter)) {
            return new ArrayList<>();
        }

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
            List<Patient> patients = patientService.getPatientsOfSamples(studyIds, sampleIds);
            result = addDataSourceToClinicalDataCounts(result, patients.size());
        }
        
        return result;
    }

    // Remove DATA_SOURCE if it is being used to filter the cohort
    private boolean extractAndVerifyDataSourceFilter(StudyViewFilter studyViewFilter) {
        
        List<ClinicalDataFilter> clinicalDataFilters = studyViewFilter.getClinicalDataFilters();
        if (clinicalDataFilters == null) {
            return true;
        }
        Optional<ClinicalDataFilter> dataSourceFilter = clinicalDataFilters
            .stream()
            .filter(filt -> filt.getAttributeId().equals("DATA_SOURCE"))
            .findFirst();
        if (dataSourceFilter.isPresent()) {
            // Remove the filter so the Study View service classes don't see it
            clinicalDataFilters.removeIf(filt -> filt.getAttributeId().equals("DATA_SOURCE"));

            // Ensure that at least one of the values requested matches this data source
            boolean thisSourceIncluded = dataSourceFilter
                .get()
                .getValues()
                .stream()
                .anyMatch(val -> val.getValue().equals(dataSourceId));
            if (!thisSourceIncluded) {
                return false;
            }
        }
        
        return true;
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
        StudyViewFilter studyViewFilter = interceptedClinicalDataBinCountFilter.getStudyViewFilter();
        if (!extractAndVerifyDataSourceFilter(studyViewFilter)) {
            return new ArrayList<>();
        }

        return clinicalDataBinUtil.fetchClinicalDataBinCounts(
            DataBinMethod.STATIC,
            interceptedClinicalDataBinCountFilter,
            // we don't need to remove filter again since we already did it in the previous step 
            false
        );
    }
}