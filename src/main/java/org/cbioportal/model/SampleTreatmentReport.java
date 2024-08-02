package org.cbioportal.model;

import java.util.Collection;

public record SampleTreatmentReport(int totalPatientCount, int totalSampleCount, Collection<SampleTreatment> treatments) {
}
