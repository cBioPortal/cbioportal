package org.cbioportal.model;

import javax.validation.constraints.NotNull;

public class ResourceDefinition {

    @NotNull
    private String resourceId;
    @NotNull
    private String displayName;
    private String description;
    @NotNull
    private ResourceType resourceType;
    private String priority;
    private Boolean openByDefault;
    @NotNull
    private String cancerStudyIdentifier;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
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

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Boolean getOpenByDefault() {
        return openByDefault;
    }

    public void setOpenByDefault(Boolean openByDefault) {
        this.openByDefault = openByDefault;
    }

    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }

}
