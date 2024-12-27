package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.persistence.fedapi.FederatedDataSource;
import org.cbioportal.persistence.fedapi.FederatedDataSourceImpl;
import org.cbioportal.persistence.fedapi.FederatorConfig;
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
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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

    @Value("#{'${fed.datasource.study-ids:}'.split(',')}")
    private List<String> dataSourceStudies;
    
    @Autowired
    private FederatorConfig federatorConfig;
    
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
                return aggResultsFromDifferentSources(
                    s -> s.fetchClinicalAttributes()
                );
            } catch (Exception e) {
                throw new FederationException("Failed to fetch clinical attributes", e);
            }
        } else if (federationMode == FederationMode.DATASOURCE) {
            return clinicalAttributeService.fetchClinicalAttributes(this.dataSourceStudies, "SUMMARY");
        } else {
            throw new FederationException("Federation is disabled");
        }
    }
    
    private <T> List<T> aggResultsFromDifferentSources(
        Function<FederatedDataSource, CompletableFuture<List<T>>> apiFunc
    ) throws Exception {
        // Each list = results from one data source
        // WhenAll's over the list of tasks to get a CF<List<List<CA>>>
        // Awaits this to get a List<List<CA>>
        // Once we have the list of lists... for now just flatten to merge them (assume each source has unique studies)

        List<FederatedDataSource> sources = federatorConfig.getSources()
            .stream()
            .<FederatedDataSource>map(inf -> new FederatedDataSourceImpl(inf))
            .toList();

        List<CompletableFuture<List<T>>> futures = sources.stream()
            .map(apiFunc::apply)
            .toList();

        CompletableFuture<List<List<T>>> combinedFuture =
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                    futures.stream()
                        .map(CompletableFuture::join) // Join each future result
                        .collect(Collectors.toList()) // Collect into List<List<ClinicalAttribute>>
                );
        
        List<List<T>> results = combinedFuture.get();
        List<T> flattened = results.stream()
            .flatMap(List::stream) // Flatten each sublist
            .collect(Collectors.toList());
        return flattened;
    }
    
    @Override
    public List<ClinicalDataCountItem> fetchClinicalDataCounts(
        ClinicalDataCountFilter filter
    ) throws FederationException {
        if (federationMode == FederationMode.FEDERATOR) {
            try {
                return aggResultsFromDifferentSources(
                    s -> s.fetchClinicalDataCounts(filter)
                );
            } catch (Exception e) {
                throw new FederationException("Failed to fetch clinical data counts", e);
            }
        } else if (federationMode == FederationMode.DATASOURCE) {
            // TODO: replicate the logic for cacheableClinicalDataCounts here
            filter.getStudyViewFilter().setStudyIds(dataSourceStudies);
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
                return aggResultsFromDifferentSources(
                    s -> s.fetchClinicalDataBinCounts(filter)
                );
            } catch (Exception e) {
                throw new FederationException("Failed to fetch clinical data bin counts", e);
            }
        } else if (federationMode == FederationMode.DATASOURCE) {
            // TODO: replicate the logic for cacheableClinicalDataBinCounts here
            filter.getStudyViewFilter().setStudyIds(dataSourceStudies);
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
