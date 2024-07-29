package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SampleIdentifier implements Serializable {

    private String sampleId;
    private String studyId;
    private boolean isFilteredOut = false;
    private List<String> allAttributeIds;
    private String attributeId;
    private List<String> attributeIdsWithNA = new ArrayList<>();

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

    public boolean getIsFilteredOut() {
        return isFilteredOut;
    }

    public void setIsFilteredOut(boolean isFilteredOut) {
        this.isFilteredOut = isFilteredOut;
    }

    public List<String> getAllAttributeIds() {
        return allAttributeIds;
    }

    public void setAllAttributeIds(List<String> allAttributeIds) {
        this.allAttributeIds = allAttributeIds;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    public List<String> getAttributeIdsWithNA() {
        return attributeIdsWithNA;
    }

    public void setAttributeIdsWithNA(List<String> attributeIds) {
        this.attributeIdsWithNA = attributeIds;
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
