package org.cbioportal.persistence.summary;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "summary")
public class SummaryApiConfig {

    private List<SummaryServer> servers = new ArrayList<>();

    public List<SummaryServer> getServers() {
        return servers;
    }
    
    public void setServers(List<SummaryServer> servers) {
        this.servers = servers;
    }
    
    public static class SummaryServer {

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
}
