package org.cbioportal.model;

import java.io.Serializable;

public class MolecularProfileSampleCount implements Serializable {

    private Integer numberOfMutationProfiledSamples;
	private Integer numberOfMutationUnprofiledSamples;
	private Integer numberOfCNAProfiledSamples;
    private Integer numberOfCNAUnprofiledSamples;

	public Integer getNumberOfMutationProfiledSamples() {
		return numberOfMutationProfiledSamples;
	}

	public void setNumberOfMutationProfiledSamples(Integer numberOfMutationProfiledSamples) {
		this.numberOfMutationProfiledSamples = numberOfMutationProfiledSamples;
	}

	public Integer getNumberOfMutationUnprofiledSamples() {
		return numberOfMutationUnprofiledSamples;
	}

	public void setNumberOfMutationUnprofiledSamples(Integer numberOfMutationUnprofiledSamples) {
		this.numberOfMutationUnprofiledSamples = numberOfMutationUnprofiledSamples;
	}

	public Integer getNumberOfCNAProfiledSamples() {
		return numberOfCNAProfiledSamples;
	}

	public void setNumberOfCNAProfiledSamples(Integer numberOfCNAProfiledSamples) {
		this.numberOfCNAProfiledSamples = numberOfCNAProfiledSamples;
	}

	public Integer getNumberOfCNAUnprofiledSamples() {
		return numberOfCNAUnprofiledSamples;
	}

	public void setNumberOfCNAUnprofiledSamples(Integer numberOfCNAUnprofiledSamples) {
		this.numberOfCNAUnprofiledSamples = numberOfCNAUnprofiledSamples;
	}
}
