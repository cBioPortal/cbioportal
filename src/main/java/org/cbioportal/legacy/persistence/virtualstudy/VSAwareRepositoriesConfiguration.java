package org.cbioportal.legacy.persistence.virtualstudy;

import org.cbioportal.legacy.persistence.StudyRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "vs_mode", havingValue = "true")
public class VSAwareRepositoriesConfiguration {
    @Primary
    @Bean
    public StudyRepository studyRepository(
        VirtualStudyService virtualStudyService,
        StudyRepository studyRepository
    ) {
        return new VSAwareStudyRepository(virtualStudyService, studyRepository);
    }
}
