package org.cbioportal.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

public class MutationCount extends UniqueKeyBase {

    @NotNull
    private String molecularProfileId;
    @NotNull
    private String sampleId;
    @NotNull
    private String patientId;
    @NotNull
    private String studyId;
    @NotNull
    private Integer mutationCount;

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
    }
    
    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public Integer getMutationCount() {
        return mutationCount;
    }

    public void setMutationCount(Integer mutationCount) {
        this.mutationCount = mutationCount;
    }
}
