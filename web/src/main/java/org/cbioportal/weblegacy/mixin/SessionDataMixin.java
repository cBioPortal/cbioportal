package org.cbioportal.weblegacy.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;

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
