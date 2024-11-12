package org.cbioportal.persistence.fedapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "fed")
public class FederatedDataSourceConfig {

    private List<FederatedDataSourceInfo> dataSources = new ArrayList<>();

    public List<FederatedDataSourceInfo> getDataSources() {
        return dataSources;
    }
    
    public void setDataSources(List<FederatedDataSourceInfo> dataSources) {
        this.dataSources = dataSources;
    }
}
