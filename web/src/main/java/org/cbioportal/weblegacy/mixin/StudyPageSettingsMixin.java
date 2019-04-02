package org.cbioportal.weblegacy.mixin;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class StudyPageSettingsMixin {
    @JsonIgnore
    private String version;
    @JsonIgnore
    private String owner;
    @JsonIgnore
    private Long created;
    @JsonIgnore
    private Long lastUpdated;
    @JsonIgnore
    private Set<String> users;

}
