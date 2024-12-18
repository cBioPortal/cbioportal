package org.cbioportal.persistence.fedapi;


import java.util.ArrayList;
import java.util.List;

public class FederatedDataSourceInfo {

    private String name;
    private String baseUrl;
    
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
}
