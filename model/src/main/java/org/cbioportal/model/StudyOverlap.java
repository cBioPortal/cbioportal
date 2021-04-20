package org.cbioportal.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 
 */
public class StudyOverlap {
    private String studyId;
    private Set<String> overlappingStudyIds;
    
    public StudyOverlap(){}

    public StudyOverlap(String studyId) {
        this.studyId = studyId;
        overlappingStudyIds = new HashSet<>();
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public Set<String> getOverlappingStudyIds() {
        return overlappingStudyIds;
    }

    public void setOverlappingStudyIds(Set<String> overlappingStudyIds) {
        this.overlappingStudyIds = overlappingStudyIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudyOverlap)) return false;
        StudyOverlap that = (StudyOverlap) o;
        return getStudyId().equals(that.getStudyId()) && getOverlappingStudyIds().equals(that.getOverlappingStudyIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStudyId(), getOverlappingStudyIds());
    }
}
