package org.cbioportal.persistence.summary;

import java.util.List;

public class SummaryServer {

    private final String name;
    private final String baseUrl;
    private final List<String> studyIds;
    private final List<String> supportedEndpoints;

    public SummaryServer(String name, String baseUrl, List<String> studyIds, List<String> supportedEndpoints) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.studyIds = studyIds;
        this.supportedEndpoints = supportedEndpoints;
    }

    public String getName() {
        return name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public List<String> getStudyIds() {
        return studyIds;
    }

    public List<String> getSupportedEndpoints() {
        return supportedEndpoints;
    }
}
