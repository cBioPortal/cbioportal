package org.cbioportal.model;

import java.util.Objects;

public class TreatmentSequenceEdge {
    private final TreatmentSequenceNode from, to;
    private int count;

    public TreatmentSequenceEdge(TreatmentSequenceNode from, TreatmentSequenceNode to) {
        this.from = from;
        this.to = to;
        count = 0;
    }

    public TreatmentSequenceNode getFrom() {
        return from;
    }

    public TreatmentSequenceNode getTo() {
        return to;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreatmentSequenceEdge)) return false;
        TreatmentSequenceEdge that = (TreatmentSequenceEdge) o;
        return getFrom().equals(that.getFrom()) && getTo().equals(that.getTo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFrom(), getTo());
    }
}
