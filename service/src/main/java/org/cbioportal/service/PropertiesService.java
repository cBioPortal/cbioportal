package org.cbioportal.service;

import java.util.Map;

public interface PropertiesService {

    Map<String,String> getFrontendProperties();
    String getFrontendPropertiesJson();
    
}
