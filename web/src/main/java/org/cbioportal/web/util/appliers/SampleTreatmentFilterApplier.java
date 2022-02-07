package org.cbioportal.web.util.appliers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.filter.AndedSampleTreatmentFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SampleTreatmentFilterApplier extends StudyViewSubFilterApplier {
    @Autowired
    TreatmentService treatmentService;

    @Autowired
    TreatmentRowExtractor treatmentRowExtractor;

    @Override
    public List<SampleIdentifier> filter (
        List<SampleIdentifier> identifiers,
        StudyViewFilter filter
    ) {
        AndedSampleTreatmentFilters filters = filter.getSampleTreatmentFilters();
        
        List<String> sampleIds = identifiers.stream()
            .map(SampleIdentifier::getSampleId)
            .collect(Collectors.toList());
        List<String> studyIds = identifiers.stream()
            .map(SampleIdentifier::getStudyId)
            .collect(Collectors.toList());

        Map<String, Set<String>> rows = 
            treatmentService.getAllSampleTreatmentRows(sampleIds, studyIds, ClinicalEventKeyCode.Agent)
            .stream()
            .collect(Collectors.toMap(SampleTreatmentRow::key, treatmentRowExtractor::extractSamples));

        return identifiers.stream()
            .filter(id -> filters.filter(id, rows))
            .collect(Collectors.toList());
    }

    @Override
    public boolean shouldApplyFilter(StudyViewFilter studyViewFilter) {
        return (
            studyViewFilter.getSampleTreatmentFilters() != null &&
            !studyViewFilter.getSampleTreatmentFilters().getFilters().isEmpty()
        );
    }
}
