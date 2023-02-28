package org.cbioportal.service.exception;

public class ResourceDefinitionNotFoundException extends Exception {

    private String studyId;
    private String resourceId;

    public ResourceDefinitionNotFoundException(String studyId, String resourceId) {
        super();
        this.studyId = studyId;
        this.resourceId = resourceId;
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

}