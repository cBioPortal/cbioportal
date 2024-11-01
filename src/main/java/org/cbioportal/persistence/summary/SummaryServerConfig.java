package org.cbioportal.persistence.summary;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "servers")
public class SummaryServerConfig {

    private final List<SummaryServer> servers;
    
    public SummaryServerConfig(List<SummaryServer> servers) {
        this.servers = servers;
    }

    public List<SummaryServer> getServers() {
        return servers;
    }
}
