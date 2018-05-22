package org.cbioportal.model;

import java.io.Serializable;

public class MolecularProfileSampleCount implements Serializable {

    private Integer numberOfProfiledSamples;
    private Integer numberOfUnprofiledSamples;

	public Integer getNumberOfProfiledSamples() {
		return numberOfProfiledSamples;
	}

	public void setNumberOfProfiledSamples(Integer numberOfProfiledSamples) {
		this.numberOfProfiledSamples = numberOfProfiledSamples;
	}

	public Integer getNumberOfUnprofiledSamples() {
		return numberOfUnprofiledSamples;
	}

	public void setNumberOfUnprofiledSamples(Integer numberOfUnprofiledSamples) {
		this.numberOfUnprofiledSamples = numberOfUnprofiledSamples;
	}
}
