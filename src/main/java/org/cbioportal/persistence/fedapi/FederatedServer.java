package org.cbioportal.persistence.fedapi;


import java.util.ArrayList;
import java.util.List;

public class FederatedServer {

    private String name;
    private String baseUrl;
    private List<String> studyIds = new ArrayList<>();
    private List<String> supportedEndpoints = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<String> getStudyIds() {
        return studyIds;
    }

    public void setStudyIds(List<String> studyIds) {
        this.studyIds = studyIds;
    }

    public List<String> getSupportedEndpoints() {
        return supportedEndpoints;
    }

    public void setSupportedEndpoints(List<String> supportedEndpoints) {
        this.supportedEndpoints = supportedEndpoints;
    }
}
