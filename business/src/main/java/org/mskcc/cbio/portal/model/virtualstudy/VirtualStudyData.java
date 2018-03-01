package org.mskcc.cbio.portal.model.virtualstudy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class VirtualStudyData {

	private String name;

	private String description;

	private Set<VirtualStudySamples> studies;

	private VirtualStudyFilters filters;

	private String owner = "anonymous";

	private Set<String> origin = new HashSet<>();

	private Long created = System.currentTimeMillis();
	
	private Set<String> users = new HashSet<>();
	
	private Float version = 1.0f;

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
		if(origin == null || origin.size() == 0) {
		    return studies.stream().map(map -> map.getId()).collect(Collectors.toSet());
		}
		return origin;
	}

	public void setOrigin(Set<String> origin) {
		this.origin = origin;
	}

	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public Set<String> getUsers() {
		return users;
	}

	public void setUsers(Set<String> users) {
		this.users = users;
	}
	
	public Float getVersion() {
		return version;
	}

	public void setVersion(Float version) {
		this.version = version;
	}
}
