package org.mskcc.cbio.portal.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CohortStudyCasesMap {
	@JsonProperty
	private String studyID;
	@JsonProperty
	private Set<String> samples;
	@JsonProperty
	private Set<String> patients;

	public String getStudyID() {
		return studyID;
	}

	public void setStudyID(String studyID) {
		this.studyID = studyID;
	}

	public Set<String> getSamples() {
		return samples;
	}

	public void setSamples(Set<String> samples) {
		this.samples = samples;
	}

	public Set<String> getPatients() {
		return patients;
	}

	public void setPatients(Set<String> patients) {
		this.patients = patients;
	}

	@Override
	public String toString() {
		return "CohortStudyCasesMap [studyID=" + studyID + ", samples=" + samples + ", patients=" + patients + "]";
	}

}
