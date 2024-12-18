package org.cbioportal.persistence.fedapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "fed")
public class FederatedDataSourceConfig {

    private List<FederatedDataSourceInfo> sources = new ArrayList<>();

    public List<FederatedDataSourceInfo> getSources() {
        return sources;
    }
    
    public void setSources(List<FederatedDataSourceInfo> sources) {
        this.sources = sources;
    }
}
