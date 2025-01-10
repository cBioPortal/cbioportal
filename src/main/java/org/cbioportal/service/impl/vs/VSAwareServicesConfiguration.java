package org.cbioportal.service.impl.vs;

import org.cbioportal.service.ReadPermissionService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "vs_mode", havingValue = "true")
public class VSAwareServicesConfiguration {
    
    @Autowired
    private SessionServiceRequestHandler sessionServiceRequestHandler;
    
    @Autowired
    private ReadPermissionService readPermissionService;
    
    @Primary
    @Bean
    public StudyService studyService(StudyService studyService) {
        return new VSAwareStudyServiceImpl(studyService, sessionServiceRequestHandler, readPermissionService);
    }
}
