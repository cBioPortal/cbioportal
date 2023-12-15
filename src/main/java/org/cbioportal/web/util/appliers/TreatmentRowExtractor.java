package org.cbioportal.web.util.appliers;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.TreatmentRow;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TreatmentRowExtractor {
    public Set<String> extractSamples(TreatmentRow row) {
        return row.getSamples().stream()
            .map(ClinicalEventSample::key)
            .collect(Collectors.toSet());
    }
}
