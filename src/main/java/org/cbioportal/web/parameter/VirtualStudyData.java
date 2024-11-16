package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualStudyData implements Serializable {

    private String name;
    private String description;
    private Set<VirtualStudySamples> studies;
    private StudyViewFilter studyViewFilter;
    private Float version = 1.0f;
    private String owner = "anonymous";
    private Set<String> origin = new HashSet<>();
    private Long created = System.currentTimeMillis();
    private Long lastUpdated = System.currentTimeMillis();
    private Set<String> users = new HashSet<>();

    private String typeOfCancerId;
    private String pmid;

    private Boolean dynamic;

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

    public Set<String> getOrigin() {
        if (this.origin == null || this.origin.size() == 0) {
            return studies.stream().map(map -> map.getId()).collect(Collectors.toSet());
        }
        return this.origin;
    }

    public Float getVersion() {
        return version;
    }

    public void setVersion(Float version) {
        this.version = version;
    }

    public StudyViewFilter getStudyViewFilter() {
        return studyViewFilter;
    }

    public void setStudyViewFilter(StudyViewFilter studyViewFilter) {
        this.studyViewFilter = studyViewFilter;
    }

    public String getTypeOfCancerId() {
        return typeOfCancerId;
    }

    public void setTypeOfCancerId(String typeOfCancerId) {
        this.typeOfCancerId = typeOfCancerId;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public Boolean getDynamic() {
        return dynamic;
    }

    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
    }
}
