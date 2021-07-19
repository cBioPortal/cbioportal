package org.cbioportal.model;

import java.util.Objects;

public class TreatmentSequenceNode {
    private final String treatment;
    private final int index;

    public TreatmentSequenceNode(String treatment, int index) {
        this.treatment = treatment;
        this.index = index;
    }

    public String getTreatment() {
        return treatment;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreatmentSequenceNode)) return false;
        TreatmentSequenceNode that = (TreatmentSequenceNode) o;
        return getIndex() == that.getIndex() && getTreatment().equals(that.getTreatment());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTreatment(), getIndex());
    }
}
