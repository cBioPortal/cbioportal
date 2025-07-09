package org.cbioportal.legacy.persistence.virtualstudy;

import org.cbioportal.legacy.persistence.AlterationRepository;
import org.cbioportal.legacy.persistence.ClinicalAttributeRepository;
import org.cbioportal.legacy.persistence.ClinicalDataRepository;
import org.cbioportal.legacy.persistence.ClinicalEventRepository;
import org.cbioportal.legacy.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.legacy.persistence.GenePanelRepository;
import org.cbioportal.legacy.persistence.GenericAssayRepository;
import org.cbioportal.legacy.persistence.MolecularProfileRepository;
import org.cbioportal.legacy.persistence.PatientRepository;
import org.cbioportal.legacy.persistence.SampleListRepository;
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
  public VSAwareClinicalDataRepository clinicalDataRepository(
      ClinicalDataRepository clinicalDataRepository,
      VSAwarePatientRepository vsAwarePatientRepository) {
    return new VSAwareClinicalDataRepository(
        virtualStudyService, clinicalDataRepository, vsAwarePatientRepository);
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

  @Primary
  @Bean
  public VSAwareGenericAssayRepository genericAssayRepository(
      GenericAssayRepository genericAssayRepository) {
    return new VSAwareGenericAssayRepository(virtualStudyService, genericAssayRepository);
  }

  @Primary
  @Bean
  public VSAwareAlterationRepository alterationRepository(
      AlterationRepository alterationRepository) {
    return new VSAwareAlterationRepository(virtualStudyService, alterationRepository);
  }

  @Primary
  @Bean
  public VSAwarePatientRepository patientRepository(PatientRepository patientRepository) {
    return new VSAwarePatientRepository(virtualStudyService, patientRepository);
  }

  @Primary
  @Bean
  public VSAwareClinicalEventRepository clinicalEventRepository(
      ClinicalEventRepository clinicalEventRepository,
      VSAwarePatientRepository vsAwarePatientRepository) {
    return new VSAwareClinicalEventRepository(
        virtualStudyService, clinicalEventRepository, vsAwarePatientRepository);
  }

  @Primary
  @Bean
  public VSAwareGenePanelRepository genePanelRepository(
      GenePanelRepository genePanelRepository,
      VSAwareMolecularProfileRepository molecularProfileRepository,
      VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareGenePanelRepository(
        virtualStudyService, genePanelRepository, molecularProfileRepository, sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareSampleListRepository sampleListRepository(
      SampleListRepository sampleListRepository) {
    return new VSAwareSampleListRepository(virtualStudyService, sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareDiscreteCopyNumberRepository discreteCopyNumberRepository(
      DiscreteCopyNumberRepository discreteCopyNumberRepository,
      VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareDiscreteCopyNumberRepository(
        virtualStudyService, discreteCopyNumberRepository, sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareMutationRepository mutationRepository(
      org.cbioportal.legacy.persistence.MutationRepository mutationRepository,
      VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareMutationRepository(
        virtualStudyService, mutationRepository, sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareStructuralVariantRepository vsStructuralVariantRepository(
      org.cbioportal.legacy.persistence.StructuralVariantRepository structuralVariantRepository) {
    return new VSAwareStructuralVariantRepository(virtualStudyService, structuralVariantRepository);
  }

  @Primary
  @Bean
  public VSAwareCopyNumberSegmentRepository vsAwareCopyNumberSegmentRepository(
      org.cbioportal.legacy.persistence.CopyNumberSegmentRepository copyNumberSegmentRepository,
      VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareCopyNumberSegmentRepository(
        virtualStudyService, copyNumberSegmentRepository, sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareMolecularDataRepository molecularDataRepository(
      org.cbioportal.legacy.persistence.MolecularDataRepository molecularDataRepository,
      SampleRepository sampleRepository) {
    return new VSAwareMolecularDataRepository(
        virtualStudyService, molecularDataRepository, sampleRepository);
  }

  @Primary
  @Bean
  public VSAwareVariantCountRepository variantCountRepository(
      org.cbioportal.legacy.persistence.VariantCountRepository variantCountRepository) {
    return new VSAwareVariantCountRepository(virtualStudyService, variantCountRepository);
  }
}
