package org.cbioportal.service.exception;

public class SampleNotFoundException extends Exception {

    private String studyId;
    private String sampleId;

    public SampleNotFoundException(String studyId, String sampleId) {
        super();
        this.studyId = studyId;
        this.sampleId = sampleId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }
}
