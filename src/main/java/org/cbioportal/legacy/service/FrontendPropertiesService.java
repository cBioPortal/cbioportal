package org.cbioportal.legacy.service;

import java.util.Map;

public interface FrontendPropertiesService {

    String getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty property);
    Map<String,String> getFrontendProperties();
    
}
