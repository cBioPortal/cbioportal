package org.mskcc.cbio.portal.model;

public class ResourceDefinition {

    public static final String STUDY_RESOURCE_TYPE = "STUDY";
    public static final String SAMPLE_RESOURCE_TYPE = "SAMPLE";
    public static final String PATIENT_RESOURCE_TYPE = "PATIENT";
	public static final String MISSING = "MISSING";

    private String resourceId;
    private String displayName;
    private String description;
    private String resourceType;
    private boolean openByDefault;
    private Integer priority;
    private Integer cancerStudyId;

    public ResourceDefinition(String resourceId, String displayName,
            String description,
                             String resourceType, boolean openByDefault, Integer priority, Integer cancerStudyId) {
        this.resourceId = resourceId;
		this.displayName = displayName;
		this.description = description;
        this.resourceType = resourceType;
        this.openByDefault = openByDefault;
        this.priority = priority;
        this.cancerStudyId = cancerStudyId;
    }

    @Override
    public String toString() {
        return "ResourceDefinition[" +
            resourceId + "," +
            displayName + "," +
            description + "," +
            priority + "," +
            resourceType + "," +
            String.valueOf(cancerStudyId) + "]";
    }

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

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

	public boolean isOpenByDefault() {
		return openByDefault;
	}

	public void setOpenByDefault(boolean openByDefault) {
		this.openByDefault = openByDefault;
	}

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    public Integer getCancerStudyId() {
        return cancerStudyId;
    }
    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }
}
