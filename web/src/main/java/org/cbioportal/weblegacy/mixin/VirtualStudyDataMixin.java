package org.cbioportal.weblegacy.mixin;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class VirtualStudyDataMixin {

	@JsonIgnore
	private String owner;
	@JsonIgnore
	private Long created;
	@JsonIgnore
	private Float version;
	@JsonIgnore
	private Set<String> users;
    @JsonIgnore
    private Long lastUpdated;
}
