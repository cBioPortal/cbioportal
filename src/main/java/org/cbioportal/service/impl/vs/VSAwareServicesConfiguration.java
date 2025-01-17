package org.cbioportal.service.impl.vs;

import org.cbioportal.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.service.*;
import org.cbioportal.service.util.MolecularProfileUtil;
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

    @Primary
    @Bean
    public StudyService studyService(StudyService studyService, PublishedVirtualStudyService publishedVirtualStudyService, ReadPermissionService readPermissionService) {
        return new VSAwareStudyServiceImpl(studyService, publishedVirtualStudyService, readPermissionService);
    }

    @Primary
    @Bean
    public ClinicalAttributeService clinicalAttributeService(ClinicalAttributeService clinicalAttributeService, PublishedVirtualStudyService publishedVirtualStudyService, SampleListService sampleListService) {
        return new VSAwareClinicalAttributeService(clinicalAttributeService, publishedVirtualStudyService, sampleListService);
    }

    @Primary
    @Bean
    public MolecularProfileService molecularProfileService(MolecularProfileService molecularProfileService, PublishedVirtualStudyService publishedVirtualStudyService, MolecularProfileUtil molecularProfileUtil) {
        return new VSAwareMolecularProfileService(molecularProfileService, publishedVirtualStudyService, molecularProfileUtil);
    }
    
    @Primary
    @Bean
    CacheMapUtil cacheMapUtil(CacheMapUtil cacheMapUtil, PublishedVirtualStudyService publishedVirtualStudyService) {
        return new VSAwareCacheMapUtil(cacheMapUtil, publishedVirtualStudyService);
    }

    @Primary
    @Bean
    public SampleService sampleService(SampleService sampleService, PublishedVirtualStudyService publishedVirtualStudyService, SampleListService sampleListService) {
        return new VSAwareSampleService(sampleService, publishedVirtualStudyService, sampleListService);
    }
    
    @Primary
    @Bean
    public MutationService mutationService(MutationService mutationService, PublishedVirtualStudyService publishedVirtualStudyService) {
        return new VSAwareMutationService(mutationService, publishedVirtualStudyService);
    }
    
    @Primary
    @Bean
    public StudyViewService studyViewService(StudyViewService studyViewService, PublishedVirtualStudyService publishedVirtualStudyService) {
        return new VMAwareStudyViewService(studyViewService, publishedVirtualStudyService);
    }
}
