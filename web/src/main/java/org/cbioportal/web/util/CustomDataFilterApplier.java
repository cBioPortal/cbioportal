package org.cbioportal.web.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.MultiKeyMap;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.PatientService;
import org.cbioportal.session_service.domain.SessionType;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.CustomDataSession;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomDataFilterApplier extends ClinicalDataEqualityFilterApplier {

    @Autowired
    public CustomDataFilterApplier(PatientService patientService, ClinicalDataService clinicalDataService,
            StudyViewFilterUtil studyViewFilterUtil) {
        super(patientService, clinicalDataService, studyViewFilterUtil);
    }

    @Autowired
    private SessionServiceRequestHandler sessionServiceRequestHandler;

    @Override
    public List<SampleIdentifier> apply(List<SampleIdentifier> sampleIdentifiers,
            List<ClinicalDataFilter> customDataFilters, Boolean negateFilters) {
        if (!customDataFilters.isEmpty() && !sampleIdentifiers.isEmpty()) {

            List<CompletableFuture<CustomDataSession>> postFutures = customDataFilters.stream()
                    .map(clinicalDataFilter -> {
                        return CompletableFuture.supplyAsync(() -> {
                            try {
                                return (CustomDataSession) sessionServiceRequestHandler
                                        .getSession(SessionType.custom_data, clinicalDataFilter.getAttributeId());
                            } catch (Exception e) {
                                return null;
                            }
                        });
                    }).collect(Collectors.toList());

            CompletableFuture.allOf(postFutures.toArray(new CompletableFuture[postFutures.size()])).join();

            Map<String, CustomDataSession> customDataSessionById = postFutures
                    .stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(CustomDataSession::getId, Function.identity()));

            MultiKeyMap clinicalDataMap = new MultiKeyMap();

            customDataSessionById.values()
            .stream()
            .forEach(customDataSession -> {
                customDataSession.getData().getData().forEach(datum -> {
                    String value = datum.getValue().toUpperCase();
                    if (value.equals("NAN") || value.equals("N/A")) {
                        value = "NA";
                    }
                    clinicalDataMap.put(datum.getStudyId(), datum.getSampleId(), customDataSession.getId(), value);
                });
            });

            List<SampleIdentifier> newSampleIdentifiers = new ArrayList<>();

            sampleIdentifiers.forEach(sampleIdentifier -> {
                int count = apply(customDataFilters, clinicalDataMap,
                        sampleIdentifier.getSampleId(), sampleIdentifier.getStudyId(), negateFilters);

                if (count == customDataFilters.size()) {
                    newSampleIdentifiers.add(sampleIdentifier);
                }
            });

            return newSampleIdentifiers;
        }
        return sampleIdentifiers;
    }

}
