package org.cbioportal.model;

import java.io.Serializable;

public class MolecularProfileSampleCount implements Serializable {

    private Integer numberOfMutationProfiledSamples;
	private Integer numberOfMutationUnprofiledSamples;
	private Integer numberOfCNAProfiledSamples;
    private Integer numberOfCNAUnprofiledSamples;
    private Integer numberOfCNSegmentSamples; // TODO this is not actually molecular profile data, we should probably change the name of the model
    private Integer numberOfFusionProfiledSamples;
    private Integer numberOfFusionUnprofiledSamples;

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

    public Integer getNumberOfCNSegmentSamples() {
        return numberOfCNSegmentSamples;
    }

    public void setNumberOfCNSegmentSamples(Integer numberOfCNSegmentSamples) {
        this.numberOfCNSegmentSamples = numberOfCNSegmentSamples;
    }

    public void setNumberOfFusionProfiledSamples(Integer numberOfFusionProfiledSamples) {
        this.numberOfFusionProfiledSamples = numberOfFusionProfiledSamples;
    }

    public Integer getNumberOfFusionProfiledSamples() {
        return numberOfFusionProfiledSamples;
    }

    public void setNumberOfFusionUnprofiledSamples(Integer numberOfFusionUnprofiledSamples) {
        this.numberOfFusionUnprofiledSamples = numberOfFusionUnprofiledSamples;
    }

    public Integer getNumberOfFusionUnprofiledSamples() {
        return numberOfFusionUnprofiledSamples;
    }
}
