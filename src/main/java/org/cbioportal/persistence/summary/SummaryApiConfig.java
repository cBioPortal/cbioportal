package org.cbioportal.persistence.summary;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
}
