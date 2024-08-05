package org.cbioportal.model;

import java.util.Collection;

public record SampleTreatmentReport(int totalSampleCount, Collection<SampleTreatment> treatments) {
}
