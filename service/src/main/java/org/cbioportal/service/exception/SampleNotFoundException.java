package org.cbioportal.service.exception;

public class SampleNotFoundException extends Exception {

    private String sampleId;

    public SampleNotFoundException(String sampleId) {
        super();
        this.sampleId = sampleId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }
}
