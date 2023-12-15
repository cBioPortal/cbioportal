package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.Objects;

public class SampleIdentifier implements Serializable {

    private String sampleId;
    private String studyId;

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    @Override
    public String toString() {
        return getSampleId() + getStudyId();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof SampleIdentifier)) {
            return false;
        }
        SampleIdentifier user = (SampleIdentifier) o;
        return Objects.equals(sampleId, user.sampleId) && Objects.equals(studyId, user.studyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sampleId, studyId);
    }
}
