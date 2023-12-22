package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.Set;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "page", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(value = ResultsPageSettings.class, name = "results_view"),
    @JsonSubTypes.Type(value = StudyPageSettings.class, name = "study_view") 
})
@JsonInclude(Include.NON_NULL)
public abstract class PageSettingsData implements Serializable {

    @NotNull
    private SessionPage page;
    private String owner = "anonymous";
    @NotNull
    private Set<String> origin;
    private Long created = System.currentTimeMillis();
    private Long lastUpdated = System.currentTimeMillis();

    public SessionPage getPage() {
        return page;
    }

    public void setPage(SessionPage page) {
        this.page = page;
    }

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

    public Set<String> getOrigin() {
        return this.origin;
    }

}
