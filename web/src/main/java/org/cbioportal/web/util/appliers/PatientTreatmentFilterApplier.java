package org.cbioportal.web.util.appliers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.filter.AndedPatientTreatmentFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PatientTreatmentFilterApplier {
    @Autowired
    TreatmentService treatmentService;
    
    public List<SampleIdentifier> filter(
        List<SampleIdentifier> identifiers,
        AndedPatientTreatmentFilters filters
    ) {
        List<String> sampleIds = identifiers.stream()
            .map(SampleIdentifier::getSampleId)
            .collect(Collectors.toList());

        List<String> studyIds = identifiers.stream()
            .map(SampleIdentifier::getStudyId)
            .collect(Collectors.toList());

        Map<String, Map<String, Boolean>> rows = treatmentService.getAllPatientTreatmentRows(sampleIds, studyIds)
            .stream()
            .collect(Collectors.toMap(PatientTreatmentRow::getTreatment, this::extractSamples));

        return identifiers.stream()
            .filter(i -> filters.filter(i, rows))
            .collect(Collectors.toList());
    }
    
    private Map<String, Boolean> extractSamples(PatientTreatmentRow row) {
        HashMap<String, Boolean> samples = new HashMap<>();
        row.getSamples().forEach(sample -> {
            samples.put(sample.key(), true);
        });
        
        return samples;
    }
}
