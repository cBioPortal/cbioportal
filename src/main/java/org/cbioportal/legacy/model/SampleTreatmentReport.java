package org.cbioportal.legacy.model;

import java.util.Collection;

public record SampleTreatmentReport(int totalSamples, Collection<SampleTreatmentRow> treatments) {
}
