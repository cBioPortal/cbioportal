package org.cbioportal.service.impl.vs;

import org.cbioportal.service.ReadPermissionService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

import java.util.concurrent.Executor;

@Configuration
@ConditionalOnProperty(name = "vs_mode", havingValue = "true")
public class VSAwareServicesConfiguration {
    
    @Autowired
    private SessionServiceRequestHandler sessionServiceRequestHandler;
    
    @Autowired
    private ReadPermissionService readPermissionService;
    
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return new DelegatingSecurityContextExecutor(executor);
    }

    @Primary
    @Bean
    public StudyService studyService(StudyService studyService, Executor asyncExecutor) {
        return new VSAwareStudyServiceImpl(studyService, sessionServiceRequestHandler, readPermissionService, asyncExecutor);
    }
}
