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
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "fedapi.mode", havingValue = "FEDERATOR")
public class FederatorService implements FederatedService {

    @Autowired
    private FederatorConfig federatorConfig;

    @Override
    public List<ClinicalAttribute> fetchClinicalAttributes() throws FederationException {
        try {
            return aggResultsFromDifferentSources(
                s -> s.fetchClinicalAttributes()
            );
        } catch (Exception e) {
            throw new FederationException("Failed to fetch clinical attributes", e);
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
        try {
            return aggResultsFromDifferentSources(
                s -> s.fetchClinicalDataCounts(filter)
            );
        } catch (Exception e) {
            throw new FederationException("Failed to fetch clinical data counts", e);
        }
    }

    @Override
    public List<ClinicalDataBin> fetchClinicalDataBinCounts(
        ClinicalDataBinCountFilter filter
    ) throws FederationException {
        try {
            return aggResultsFromDifferentSources(
                s -> s.fetchClinicalDataBinCounts(filter)
            );
        } catch (Exception e) {
            throw new FederationException("Failed to fetch clinical data bin counts", e);
        }
    }
}