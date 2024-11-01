package org.cbioportal.persistence.summary;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "servers")
public class SummaryServerConfig {

    private List<SummaryServer> servers;

    public List<SummaryServer> getServers() {
        return servers;
    }

    public void setServers(List<SummaryServer> servers) {
        this.servers = servers;
    }
}
