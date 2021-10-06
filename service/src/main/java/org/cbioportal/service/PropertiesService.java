package org.cbioportal.service;

import java.util.Map;

public interface PropertiesService {

    String getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty property);
    Map<String,String> getFrontendProperties();
    
}
