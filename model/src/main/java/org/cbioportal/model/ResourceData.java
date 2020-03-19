package org.cbioportal.model;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

public class ResourceData extends UniqueKeyBase implements Serializable {

    private String sampleId;
    private String patientId;
    @NotNull
    private String studyId;
    @NotNull
    private String resourceId;
    @NotNull
    private String url;
    private ResourceDefinition resourceDefinition;

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ResourceDefinition getResourceDefinition() {
        return resourceDefinition;
    }

    public void setResourceDefinition(ResourceDefinition resourceDefinition) {
        this.resourceDefinition = resourceDefinition;
    }

}
