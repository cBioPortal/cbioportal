package org.cbioportal.weblegacy.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class StudyPageSettingsMixin {

    @JsonIgnore
    private String created;
    @JsonIgnore
    private Long lastUpdated;
    @JsonIgnore
    private String owner;
    @JsonIgnore
    private String users;

}
