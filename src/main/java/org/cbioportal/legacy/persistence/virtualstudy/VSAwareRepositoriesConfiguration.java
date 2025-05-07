package org.cbioportal.legacy.persistence.virtualstudy;

import org.cbioportal.legacy.persistence.ClinicalAttributeRepository;
import org.cbioportal.legacy.persistence.MolecularProfileRepository;
import org.cbioportal.legacy.persistence.SampleRepository;
import org.cbioportal.legacy.persistence.StudyRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(name = "vs_mode", havingValue = "true")
public class VSAwareRepositoriesConfiguration {

  @Autowired VirtualStudyService virtualStudyService;

  @Primary
  @Bean
  public StudyRepository studyRepository(StudyRepository studyRepository) {
    return new VSAwareStudyRepository(virtualStudyService, studyRepository);
  }

  @Primary
  @Bean
  VirtualStudyService virtualStudiesServiceWithSilencedPublishedVirtualStudies() {
    return new SilencedPublishedVSService(virtualStudyService);
  }

  @Primary
  @Bean
  ClinicalAttributeRepository clinicalAttributeRepository(
      ClinicalAttributeRepository clinicalAttributeRepository) {
    return new VSAwareClinicalAttributeRepository(virtualStudyService, clinicalAttributeRepository);
  }

  @Primary
  @Bean
  public MolecularProfileRepository molecularProfileRepository(
      MolecularProfileRepository molecularProfileRepository) {
    return new VSAwareMolecularProfileRepository(virtualStudyService, molecularProfileRepository);
  }

  @Primary
  @Bean
  public SampleRepository sampleRepository(SampleRepository sampleRepository) {
    return new VSAwareSampleRepository(virtualStudyService, sampleRepository);
  }
}
