package org.cbioportal.web.util.appliers;

import org.cbioportal.service.ClinicalEventService;
import org.cbioportal.web.parameter.DataFilter;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public final class ClinicalEventFilterApplier implements StudyViewSubFilterApplier {
    
    @Autowired
    private ClinicalEventService clinicalEventService;
    
    @Override
    public List<SampleIdentifier> filter(@NonNull List<SampleIdentifier> toFilter, @NonNull StudyViewFilter filters) {

       if (toFilter == null || toFilter.isEmpty()) {
           return new ArrayList<>();
       }
        
       List<String> studyIds = toFilter.stream()
           .map(SampleIdentifier::getStudyId)
           .collect(Collectors.toList());
      
       List<String> sampleIds = toFilter.stream()
           .map(SampleIdentifier::getSampleId)
           .collect(Collectors.toList());
       
       Map<String, Set<String>> samplesPerEventType = clinicalEventService.getPatientsSamplesPerClinicalEventType(studyIds, sampleIds);
       
       List<ClinicalEventFilter> clinicalEventFilters = filters.getClinicalEventFilters().stream()
           .map(ClinicalEventFilter::new)
           .collect(Collectors.toList());
       
        return toFilter.stream()
            .filter(i -> applyClinicalEventFilter(i, clinicalEventFilters, samplesPerEventType))
            .collect(Collectors.toList());
    }

    boolean applyClinicalEventFilter(SampleIdentifier sampleIdentifier, List<ClinicalEventFilter> eventFilters, Map<String, Set<String>> samplesPerEventType) {
        for(ClinicalEventFilter eventFilter : eventFilters) {
            if(!eventFilter.filter(sampleIdentifier, samplesPerEventType)){
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean shouldApplyFilter(@NonNull StudyViewFilter studyViewFilter) {
        return studyViewFilter.getClinicalEventFilters() != null && !studyViewFilter.getClinicalEventFilters().isEmpty();
    }
    
    private static class ClinicalEventFilter {
        private final List<DataFilterValue> filters;
        ClinicalEventFilter(DataFilter filters) {
            this.filters = filters.getValues();
        }
        
        public boolean filter(SampleIdentifier s, Map<String, Set<String>> samplesPerEventType) {
            if(Objects.isNull(filters) || filters.isEmpty()) {
                return true;
            }
            
            for(DataFilterValue filter : filters) {
                Collection<String> samples = samplesPerEventType.get(filter.getValue());

                if(!Objects.isNull(samples) && samples.contains(s.getSampleId())) {
                    return true;
                }
            }
            
            return false;
        }
    }
}
