package org.cbioportal.legacy.service;

import org.springframework.security.core.Authentication;

import java.util.Map;

public interface FrontendPropertiesService {

    String getFrontendProperty(FrontendPropertiesServiceImpl.FrontendProperty property);
    Map<String,String> getFrontendProperties(Authentication authentication);
    
}
