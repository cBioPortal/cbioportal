package org.cbioportal.model;

import java.io.Serializable;

public class SampleListToSampleId implements Serializable {
    
    private Integer sampleListId;
    private String sampleId;

    public Integer getSampleListId() {
        return sampleListId;
    }

    public void setSampleListId(Integer sampleListId) {
        this.sampleListId = sampleListId;
    }

	public String getSampleId() {
		return sampleId;
	}

	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}
}
