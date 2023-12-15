package org.cbioportal.service.exception;

public class StudyNotFoundException extends Exception {

    private String studyId;

    public StudyNotFoundException(String studyId) {
        super();
        this.studyId = studyId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }
}
