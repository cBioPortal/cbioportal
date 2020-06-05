package org.mskcc.cbio.portal.model.virtualstudy;

import java.util.Map;
import java.util.Set;

public class VirtualStudyFilters {
	private Map<String, Set<String>> patients;

	private Map<String, Set<String>> samples;

	public Map<String, Set<String>> getPatients() {
		return patients;
	}

	public void setPatients(Map<String, Set<String>> patients) {
		this.patients = patients;
	}

	public Map<String, Set<String>> getSamples() {
		return samples;
	}

	public void setSamples(Map<String, Set<String>> samples) {
		this.samples = samples;
	}
}
