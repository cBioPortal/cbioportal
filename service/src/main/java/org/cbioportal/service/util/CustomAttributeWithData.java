package org.cbioportal.service.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomAttributeWithData implements Serializable {

    private String owner = "anonymous";
    private Set<String> origin = new HashSet<>();
    private Long created = System.currentTimeMillis();
    private Long lastUpdated = System.currentTimeMillis();
    private Set<String> users = new HashSet<>();

    @NotNull
    private String displayName;
    private String description;
    private String datatype;
    @NotNull
    private Boolean patientAttribute;
    private String priority;

    private List<CustomDataValue> data;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }

    public Set<String> getOrigin() {
        return this.origin;
    }

    public List<CustomDataValue> getData() {
        return data;
    }

    public void setData(List<CustomDataValue> data) {
        this.data = data;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public Boolean getPatientAttribute() {
        return patientAttribute;
    }

    public void setPatientAttribute(Boolean patientAttribute) {
        this.patientAttribute = patientAttribute;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

}
