package org.cbioportal.model.virtualstudy;

import java.util.Set;

public class VirtualStudyData {

	private String name;

	private String description;

	private Set<VirtualStudySamples> studies;

	private VirtualStudyFilters filters;

	private String owner;

	private Set<String> origin;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<VirtualStudySamples> getStudies() {
		return studies;
	}

	public void setStudies(Set<VirtualStudySamples> studies) {
		this.studies = studies;
	}

	public VirtualStudyFilters getFilters() {
		return filters;
	}

	public void setFilters(VirtualStudyFilters filters) {
		this.filters = filters;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Set<String> getOrigin() {
		return origin;
	}

	public void setOrigin(Set<String> origin) {
		this.origin = origin;
	}

}
