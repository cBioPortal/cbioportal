package org.cbioportal.web.mixin;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SessionDataMixin {
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
