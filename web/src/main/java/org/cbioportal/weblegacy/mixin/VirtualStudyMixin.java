package org.cbioportal.weblegacy.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class VirtualStudyMixin {

	@JsonIgnore
	private String source;
	@JsonIgnore
	private String type;
	
}
