package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.persistence.fedapi.FederatedDataSource;
import org.cbioportal.persistence.fedapi.FederatedDataSourceImpl;
import org.cbioportal.persistence.fedapi.FederatorConfig;
import org.cbioportal.service.FederatedService;
import org.cbioportal.service.exception.FederationException;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.web.parameter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ConditionalOnProperty(name = "fedapi.mode", havingValue = "FEDERATOR", matchIfMissing = true) // TODO undo
@Primary // TODO undo
public class FederatorService implements FederatedService {

    @Autowired
    private FederatorConfig federatorConfig;

    @Override
    public List<ClinicalAttribute> fetchClinicalAttributes() throws FederationException {
        try {
            List<List<ClinicalAttribute>> results = collectResultsFromDifferentSources(
                s -> s.fetchClinicalAttributes()
            );
            return results.stream()
                .flatMap(List::stream)
                // not necessary to do a distinctBy(attributeId) -- attributes are specific to a particular study,
                // so if 2 studies have the same attribute we should leave it be.
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new FederationException("Failed to fetch clinical attributes", e);
        }
    }

    private <TResult> List<TResult> collectResultsFromDifferentSources(
        Function<FederatedDataSource, CompletableFuture<TResult>> apiFunc
    ) throws Exception {
        // Each list = results from one data source
        // WhenAll's over the list of tasks to get a CF<List<List<CA>>>
        // Awaits this to get a List<List<CA>>
        // Once we have the list of lists... for now just flatten to merge them (assume each source has unique studies)

        List<FederatedDataSource> sources = federatorConfig.getSources()
            .stream()
            .<FederatedDataSource>map(inf -> new FederatedDataSourceImpl(inf))
            .toList();

        List<CompletableFuture<TResult>> futures = sources.stream()
            .map(apiFunc::apply)
            .toList();

        CompletableFuture<List<TResult>> combinedFuture =
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                    futures.stream()
                        .map(CompletableFuture::join) // Join each future result
                        .collect(Collectors.toList()) // Collect into List<List<ClinicalAttribute>>
                );

        List<TResult> results = combinedFuture.get();
        return results;
    }

    @Override
    public List<ClinicalDataCountItem> fetchClinicalDataCounts(
        ClinicalDataCountFilter filter
    ) throws FederationException {
        try {
            List<List<ClinicalDataCountItem>> results = collectResultsFromDifferentSources(
                s -> s.fetchClinicalDataCounts(filter)
            );
            return flattenClinicalDataCountItems(results);
        } catch (Exception e) {
            throw new FederationException("Failed to fetch clinical data counts", e);
        }
    }
    
    private List<ClinicalDataCountItem> flattenClinicalDataCountItems(
        List<List<ClinicalDataCountItem>> results
    ) {
        BinaryOperator<ClinicalDataCount> mergeClinicalDataCounts = (left, right) -> {
            if (!left.getValue().equals(right.getValue())) {
                throw new IllegalArgumentException("attempting to merge counts for different values");
            }
            
            var res = new ClinicalDataCount();
            res.setValue(left.getValue());
            res.setCount(left.getCount() + right.getCount());
            return res;
        };
        
        BinaryOperator<ClinicalDataCountItem> mergeClinicalDataCountItems = (left, right) -> {
            if (!left.getAttributeId().equals(right.getAttributeId())) {
                throw new IllegalArgumentException("attempting to merge counts for different attributes");
            }
            
            List<ClinicalDataCount> leftCounts = left.getCounts();
            List<ClinicalDataCount> rightCounts = right.getCounts();
            Stream<ClinicalDataCount> combinedCounts = Stream.concat(leftCounts.stream(), rightCounts.stream());
            Map<String, ClinicalDataCount> groupedByValue = combinedCounts.collect(
                Collectors.toMap(
                    ct -> ct.getValue(),
                    ct -> ct,
                    mergeClinicalDataCounts
                )
            );
            List<ClinicalDataCount> mergedCounts = new ArrayList<>(groupedByValue.values());
            
            var res = new ClinicalDataCountItem();
            res.setAttributeId(left.getAttributeId());
            res.setCounts(mergedCounts);
            return res;
        };
        
        // Flatten the results
        List<ClinicalDataCountItem> flattened = results.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        // Group by the clinical attribute ID for each count item
        // Merge ones the same ID
        Map<String, ClinicalDataCountItem> groupedByAttr = flattened.stream()
            .collect(
                Collectors.toMap(
                    it -> it.getAttributeId(),
                    it -> it,
                    mergeClinicalDataCountItems
                )
            );
        
        List<ClinicalDataCountItem> aggResults = new ArrayList<>(groupedByAttr.values());
        return aggResults;
    }

    @Override
    public List<ClinicalDataBin> fetchClinicalDataBinCounts(
        ClinicalDataBinCountFilter filter
    ) throws FederationException {
        try {
            List<List<ClinicalDataBin>> results = collectResultsFromDifferentSources(
                s -> s.fetchClinicalDataBinCounts(filter)
            );
            // TODO merge bins for the same range?
            return results.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new FederationException("Failed to fetch clinical data bin counts", e);
        }
    }
}