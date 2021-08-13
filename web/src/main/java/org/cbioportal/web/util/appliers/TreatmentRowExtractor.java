package org.cbioportal.web.util.appliers;

import org.cbioportal.model.TreatmentRow;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TreatmentRowExtractor {
    public Set<String> extractSamples(TreatmentRow row) {
        Set<String> samples = new HashSet<>();
        row.getSamples().forEach(sample -> {
            samples.add(sample.key());
        });

        return samples;
    }
}
