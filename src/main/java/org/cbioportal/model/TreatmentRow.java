package org.cbioportal.model;

import java.util.Set;

public interface TreatmentRow {
    public Set<ClinicalEventSample> getSamples();
}
