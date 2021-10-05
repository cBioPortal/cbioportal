package org.cbioportal.service;

import java.util.Map;

public interface PropertiesService {

    String getFrontendProperty(String property);
    Map<String,String> getFrontendProperties(String baseUrl, String username);
    String getFrontendUrl();
    
}
