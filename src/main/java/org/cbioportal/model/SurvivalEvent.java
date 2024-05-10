package org.cbioportal.model;

import java.util.List;
import java.util.function.ToIntFunction;

public class SurvivalEvent {
    List<ClinicalEvent> startClinicalEventsMeta;
    ToIntFunction<ClinicalEvent> startPositionIdentifier;
    List<ClinicalEvent> endClinicalEventsMeta;
    ToIntFunction<ClinicalEvent> endPositionIdentifier;
    List<ClinicalEvent> censoredClinicalEventsMeta;
    ToIntFunction<ClinicalEvent> censoredPositionIdentifier;

    public List<ClinicalEvent> getStartClinicalEventsMeta() {
        return startClinicalEventsMeta;
    }

    public void setStartClinicalEventsMeta(List<ClinicalEvent> startClinicalEventsMeta) {
        this.startClinicalEventsMeta = startClinicalEventsMeta;
    }

    public ToIntFunction<ClinicalEvent> getStartPositionIdentifier() {
        return startPositionIdentifier;
    }

    public void setStartPositionIdentifier(ToIntFunction<ClinicalEvent> startPositionIdentifier) {
        this.startPositionIdentifier = startPositionIdentifier;
    }

    public List<ClinicalEvent> getEndClinicalEventsMeta() {
        return endClinicalEventsMeta;
    }

    public void setEndClinicalEventsMeta(List<ClinicalEvent> endClinicalEventsMeta) {
        this.endClinicalEventsMeta = endClinicalEventsMeta;
    }

    public ToIntFunction<ClinicalEvent> getEndPositionIdentifier() {
        return endPositionIdentifier;
    }

    public void setEndPositionIdentifier(ToIntFunction<ClinicalEvent> endPositionIdentifier) {
        this.endPositionIdentifier = endPositionIdentifier;
    }

    public List<ClinicalEvent> getCensoredClinicalEventsMeta() {
        return censoredClinicalEventsMeta;
    }

    public void setCensoredClinicalEventsMeta(List<ClinicalEvent> censoredClinicalEventsMeta) {
        this.censoredClinicalEventsMeta = censoredClinicalEventsMeta;
    }

    public ToIntFunction<ClinicalEvent> getCensoredPositionIdentifier() {
        return censoredPositionIdentifier;
    }

    public void setCensoredPositionIdentifier(ToIntFunction<ClinicalEvent> censoredPositionIdentifier) {
        this.censoredPositionIdentifier = censoredPositionIdentifier;
    }
}
