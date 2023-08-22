package org.cbioportal.web.util.appliers;

import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.filter.AndedSampleTreatmentFilters;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractSampleTreatmentFilter implements StudyViewSubFilterApplier {
    @Autowired
    TreatmentService treatmentService;

    @Autowired
    TreatmentRowExtractor treatmentRowExtractor;

    @Override
    public List<SampleIdentifier> filter (
        List<SampleIdentifier> identifiers,
        StudyViewFilter filter
    ) {
    
        if (identifiers == null || identifiers.isEmpty()) {
            return new ArrayList<>();
        }

        AndedSampleTreatmentFilters filters = getFilters(filter);

        List<String> sampleIds = identifiers.stream()
            .map(SampleIdentifier::getSampleId)
            .collect(Collectors.toList());
        List<String> studyIds = identifiers.stream()
            .map(SampleIdentifier::getStudyId)
            .collect(Collectors.toList());

        Map<String, Set<String>> rows =
            treatmentService.getAllSampleTreatmentRows(sampleIds, studyIds, getCode())
                .stream()
                .collect(Collectors.toMap(SampleTreatmentRow::key, treatmentRowExtractor::extractSamples));

        return identifiers.stream()
            .filter(id -> filters.filter(id, rows))
            .collect(Collectors.toList());
    }
    
    protected abstract AndedSampleTreatmentFilters getFilters(StudyViewFilter filter);
    
    protected abstract ClinicalEventKeyCode getCode();

    @Override
    public boolean shouldApplyFilter(StudyViewFilter studyViewFilter) {
        return getFilters(studyViewFilter) != null && !getFilters(studyViewFilter).getFilters().isEmpty();
    }
}
