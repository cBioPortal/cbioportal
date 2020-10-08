package org.cbioportal.web.util.appliers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.filter.AndedSampleTreatmentFilters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SampleTreatmentFilterApplier {
    @Autowired
    TreatmentService treatmentService;
    
    public List<SampleIdentifier> filter (
        AndedSampleTreatmentFilters filters,
        List<SampleIdentifier> identifiers
    ) {
        List<String> sampleIds = identifiers.stream()
            .map(SampleIdentifier::getSampleId)
            .collect(Collectors.toList());
        List<String> studyIds = identifiers.stream()
            .map(SampleIdentifier::getStudyId)
            .collect(Collectors.toList());

        Map<String, Map<String, Boolean>> rows = treatmentService.getAllSampleTreatmentRows(sampleIds, studyIds)
            .stream()
            .collect(Collectors.toMap(SampleTreatmentRow::key, this::extractSamples));

        return identifiers.stream()
            .filter(id -> filters.filter(id, rows))
            .collect(Collectors.toList());
    }

    private Map<String, Boolean> extractSamples(SampleTreatmentRow row) {
        HashMap<String, Boolean> samples = new HashMap<>();
        row.getSamples().forEach(sample -> {
            samples.put(sample.key(), true);
        });

        return samples;
    }
}
