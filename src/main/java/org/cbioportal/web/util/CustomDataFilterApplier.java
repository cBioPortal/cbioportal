package org.cbioportal.web.util;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.cbioportal.service.CustomDataService;
import org.cbioportal.service.util.CustomDataSession;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CustomDataFilterApplier implements DataFilterApplier<ClinicalDataFilter> {

    private CustomDataService customDataService;

    private ClinicalDataEqualityFilterApplier equalityFilterApplier;
    private ClinicalDataIntervalFilterApplier intervalFilterApplier;
    
    @Autowired
    public CustomDataFilterApplier(
        CustomDataService customDataService,
        ClinicalDataEqualityFilterApplier equalityFilterApplier, 
        ClinicalDataIntervalFilterApplier intervalFilterApplier
    ) {
        this.customDataService = customDataService;
        this.equalityFilterApplier = equalityFilterApplier;
        this.intervalFilterApplier = intervalFilterApplier;
    }

    @Override
    public List<SampleIdentifier> apply(
        List<SampleIdentifier> sampleIdentifiers,
        List<ClinicalDataFilter> dataFilters,
        boolean negateFilters
    ) {
        if (dataFilters.isEmpty() || sampleIdentifiers.isEmpty()) {
            return sampleIdentifiers;
        }

        final List<String> attributeIds = dataFilters.stream()
            .map(ClinicalDataFilter::getAttributeId)
            .collect(Collectors.toList());

        final Map<String, CustomDataSession> customDataSessions = customDataService.getCustomDataSessions(attributeIds);

        Map<String, CustomDataSession> customDataSessionById = customDataSessions
            .values()
            .stream()
            .collect(Collectors.toMap(
                CustomDataSession::getId, 
                Function.identity()
            ));

        /* 
        Custom data entry with: 
        - key1: studyId; 
        - key2: sampleId; 
        - key3: sessionId.
        */
        MultiKeyMap<String, String> customDataByStudySampleSession = new MultiKeyMap<>();

        customDataSessionById.values().forEach(customDataSession -> customDataSession
            .getData()
            .getData()
            .forEach(datum -> {
                String value = datum.getValue().toUpperCase();
                if (value.equals("NAN") || value.equals("N/A")) {
                    value = "NA";
                }
                customDataByStudySampleSession.put(datum.getStudyId(), datum.getSampleId(), customDataSession.getId(), value);
            })
        );

        return filterCustomData(
            dataFilters, 
            negateFilters, 
            sampleIdentifiers, 
            customDataSessionById,
            customDataByStudySampleSession
        );
    }
    
    private List<SampleIdentifier> filterCustomData(
        List<ClinicalDataFilter> customDataFilters,
        boolean negateFilters,
        List<SampleIdentifier> sampleIdentifiers,
        Map<String, CustomDataSession> customDataSessionById,
        MultiKeyMap<String, String> clinicalDataMap
    ) {
        List<ClinicalDataFilter> equalityFilters = new ArrayList<>();
        List<ClinicalDataFilter> intervalFilters = new ArrayList<>();
        
        customDataFilters.forEach(filter -> {
            String attributeId = filter.getAttributeId();
            if (!customDataSessionById.containsKey(attributeId)) {
                return;
            }
            if (customDataSessionById
                .get(attributeId)
                .getData()
                .getDatatype()
                .equals(CustomDatatype.STRING.name())
            ) {
                equalityFilters.add(filter);
            } else {
                intervalFilters.add(filter);
            }
        });

        List<SampleIdentifier> filtered = new ArrayList<>();
        sampleIdentifiers.forEach(sampleIdentifier -> {
            int equalityFilterCount = equalityFilterApplier.apply(equalityFilters, clinicalDataMap,
                sampleIdentifier.getSampleId(), sampleIdentifier.getStudyId(), negateFilters);
            int intervalFilterCount = intervalFilterApplier.apply(intervalFilters, clinicalDataMap,
                sampleIdentifier.getSampleId(), sampleIdentifier.getStudyId(), negateFilters);
            if (equalityFilterCount == equalityFilters.size() 
                && intervalFilterCount == intervalFilters.size()
            ) {
                filtered.add(sampleIdentifier);
            }
        });
        
        return filtered;
    }
    
}
