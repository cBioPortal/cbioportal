package org.cbioportal.service.exception;

public class SampleListNotFoundException extends Exception {

    private String sampleListId;

    public SampleListNotFoundException(String sampleListId) {
        super();
        this.sampleListId = sampleListId;
    }

    public String getSampleListId() {
        return sampleListId;
    }

    public void setSampleListId(String sampleListId) {
        this.sampleListId = sampleListId;
    }
}
