package org.cbioportal.persistence.fedapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "fed")
public class FederatedServerConfig {

    private List<FederatedServer> servers = new ArrayList<>();

    public List<FederatedServer> getServers() {
        return servers;
    }
    
    public void setServers(List<FederatedServer> servers) {
        this.servers = servers;
    }
}
