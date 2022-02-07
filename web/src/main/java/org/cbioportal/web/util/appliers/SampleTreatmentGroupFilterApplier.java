package org.cbioportal.web.util.appliers;

import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.filter.AndedSampleTreatmentFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SampleTreatmentGroupFilterApplier extends StudyViewSubFilterApplier {
    @Autowired
    TreatmentService treatmentService;

    @Autowired
    TreatmentRowExtractor treatmentRowExtractor;

    public List<SampleIdentifier> filter (
        List<SampleIdentifier> identifiers,
        StudyViewFilter filter
    ) {
        AndedSampleTreatmentFilters filters = filter.getSampleTreatmentGroupFilters();
        
        List<String> sampleIds = identifiers.stream()
            .map(SampleIdentifier::getSampleId)
            .collect(Collectors.toList());
        List<String> studyIds = identifiers.stream()
            .map(SampleIdentifier::getStudyId)
            .collect(Collectors.toList());

        Map<String, Set<String>> rows = 
            treatmentService.getAllSampleTreatmentRows(sampleIds, studyIds, ClinicalEventKeyCode.AgentClass)
            .stream()
            .collect(Collectors.toMap(SampleTreatmentRow::key, treatmentRowExtractor::extractSamples));

        return identifiers.stream()
            .filter(id -> filters.filter(id, rows))
            .collect(Collectors.toList());
    }

    @Override
    public boolean shouldApplyFilter(StudyViewFilter studyViewFilter) {
        return (
            studyViewFilter.getSampleTreatmentGroupFilters() != null &&
            !studyViewFilter.getSampleTreatmentGroupFilters().getFilters().isEmpty()
        );
    }
}
