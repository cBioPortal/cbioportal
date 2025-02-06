package org.cbioportal.legacy.web.util.appliers;

import org.cbioportal.legacy.model.ClinicalEventSample;
import org.cbioportal.legacy.model.TreatmentRow;
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
