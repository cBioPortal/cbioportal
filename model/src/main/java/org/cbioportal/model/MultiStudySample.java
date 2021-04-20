package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MultiStudySample implements Serializable {
    private String sampleId;
    private Set<Integer> studyIdentifiers;

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public Set<Integer> getStudyIdentifiers() {
        return studyIdentifiers;
    }

    public void setStudyIdentifiers(Set<Integer> studyIdentifiers) {
        this.studyIdentifiers = studyIdentifiers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MultiStudySample)) return false;
        MultiStudySample that = (MultiStudySample) o;
        return getStudyIdentifiers().equals(that.getStudyIdentifiers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStudyIdentifiers());
    }
}
