package org.cbioportal.web.util.appliers;

import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.filter.AndedPatientTreatmentFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PatientTreatmentFilterApplier extends StudyViewSubFilterApplier {
    @Autowired
    TreatmentService treatmentService;
    
    @Autowired
    TreatmentRowExtractor treatmentRowExtractor;
    
    @Override
    public List<SampleIdentifier> filter(
        List<SampleIdentifier> identifiers,
        StudyViewFilter filter
    ) {
        AndedPatientTreatmentFilters filters = filter.getPatientTreatmentFilters();
        
        List<String> sampleIds = identifiers.stream()
            .map(SampleIdentifier::getSampleId)
            .collect(Collectors.toList());

        List<String> studyIds = identifiers.stream()
            .map(SampleIdentifier::getStudyId)
            .collect(Collectors.toList());

        Map<String, Set<String>> rows = 
            treatmentService.getAllPatientTreatmentRows(sampleIds, studyIds, ClinicalEventKeyCode.Agent)
            .stream()
            .collect(Collectors.toMap(PatientTreatmentRow::getTreatment, treatmentRowExtractor::extractSamples));

        return identifiers.stream()
            .filter(i -> filters.filter(i, rows))
            .collect(Collectors.toList());
    }

    @Override
    public boolean shouldApplyFilter(StudyViewFilter studyViewFilter) {
        return (
            studyViewFilter.getPatientTreatmentFilters() != null &&
            !studyViewFilter.getPatientTreatmentFilters().getFilters().isEmpty()
        );
    }
}
