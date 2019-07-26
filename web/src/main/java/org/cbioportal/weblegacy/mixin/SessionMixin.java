package org.cbioportal.weblegacy.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SessionMixin {

	@JsonIgnore
	private String source;
	@JsonIgnore
	private String type;
	@JsonIgnore
    private String checksum;
	
}
