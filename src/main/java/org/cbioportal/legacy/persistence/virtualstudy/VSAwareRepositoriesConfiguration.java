package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.function.Predicate;
import org.cbioportal.legacy.persistence.AlterationRepository;
import org.cbioportal.legacy.persistence.CancerTypeRepository;
import org.cbioportal.legacy.persistence.ClinicalAttributeRepository;
import org.cbioportal.legacy.persistence.ClinicalDataRepository;
import org.cbioportal.legacy.persistence.ClinicalEventRepository;
import org.cbioportal.legacy.persistence.CopyNumberSegmentRepository;
import org.cbioportal.legacy.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.legacy.persistence.GenePanelRepository;
import org.cbioportal.legacy.persistence.GenericAssayRepository;
import org.cbioportal.legacy.persistence.MolecularDataRepository;
import org.cbioportal.legacy.persistence.MolecularProfileRepository;
import org.cbioportal.legacy.persistence.MutationRepository;
import org.cbioportal.legacy.persistence.PatientRepository;
import org.cbioportal.legacy.persistence.ResourceDataRepository;
import org.cbioportal.legacy.persistence.ResourceDefinitionRepository;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.persistence.SampleRepository;
import org.cbioportal.legacy.persistence.StructuralVariantRepository;
import org.cbioportal.legacy.persistence.StudyRepository;
import org.cbioportal.legacy.persistence.VariantCountRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(
    name = "feature.published_virtual_study.single-sourced.backend-mode",
    havingValue = "true")
public class VSAwareRepositoriesConfiguration {

  @Autowired VirtualStudyService virtualStudyService;
  @Autowired SampleRepository sampleRepository;
  @Autowired GenePanelRepository genePanelRepository;

  /**
   * Checks if the given VirtualStudy is multi-sourced, i.e., it contains more than one study. This
   * predicate is used to split published virtual studies that will be served to the frontend as
   * published studies and ones that will be served as a regular studies by the backend.
   *
   * @param vs the VirtualStudy to check
   * @return true if the VirtualStudy is multi-sourced, false otherwise
   */
  private static boolean isMultiSourced(VirtualStudy vs) {
    return vs.getData().getStudies().size() > 1;
  }

  /** Served to the rest of the application as a primary bean. */
  @Primary
  @Bean
  VirtualStudyService multiSourcedPublishedVirtualStudiesService() {
    return new FilteredPublishedVirtualStudyService(
        virtualStudyService, VSAwareRepositoriesConfiguration::isMultiSourced);
  }

  /** Used by the Backend implementation of published virtual studies */
  @Bean
  VirtualStudyService singleSourcedPublishedVirtualStudiesService() {
    return new FilteredPublishedVirtualStudyService(
        virtualStudyService, Predicate.not(VSAwareRepositoriesConfiguration::isMultiSourced));
  }

  @Lazy @Autowired VirtualizationService virtualizationService;

  @Bean
  VirtualizationService virtualizationService(
      VSAwareMolecularProfileRepository molecularProfileRepository) {
    return new VirtualizationService(
        singleSourcedPublishedVirtualStudiesService(),
        sampleRepository,
        molecularProfileRepository);
  }

  @Primary
  @Bean
  public VSAwareStudyRepository studyRepository(
      StudyRepository studyRepository, CancerTypeRepository cancerTypeService) {
    return new VSAwareStudyRepository(virtualizationService, studyRepository, cancerTypeService);
  }

  @Primary
  @Bean
  VSAwareClinicalAttributeRepository clinicalAttributeRepository(
      ClinicalAttributeRepository clinicalAttributeRepository) {
    return new VSAwareClinicalAttributeRepository(
        virtualizationService, clinicalAttributeRepository);
  }

  @Primary
  @Bean
  public VSAwareClinicalDataRepository clinicalDataRepository(
      ClinicalDataRepository clinicalDataRepository,
      VSAwarePatientRepository patientRepository,
      VSAwareSampleRepository sampleRepository) {
    return new VSAwareClinicalDataRepository(
        virtualizationService, clinicalDataRepository, patientRepository, sampleRepository);
  }

  @Primary
  @Bean
  public VSAwareMolecularProfileRepository molecularProfileRepository(
      MolecularProfileRepository molecularProfileRepository) {
    return new VSAwareMolecularProfileRepository(
        virtualizationService, molecularProfileRepository, genePanelRepository);
  }

  @Primary
  @Bean
  public VSAwareSampleRepository sampleRepository(
      SampleRepository sampleRepository, VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareSampleRepository(
        virtualizationService, sampleRepository, sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareGenericAssayRepository genericAssayRepository(
      GenericAssayRepository genericAssayRepository) {
    return new VSAwareGenericAssayRepository(virtualizationService, genericAssayRepository);
  }

  @Primary
  @Bean
  public VSAwareAlterationRepository alterationRepository(
      AlterationRepository alterationRepository) {
    return new VSAwareAlterationRepository(virtualizationService, alterationRepository);
  }

  @Primary
  @Bean
  public VSAwarePatientRepository patientRepository(PatientRepository patientRepository) {
    return new VSAwarePatientRepository(virtualizationService, patientRepository);
  }

  @Primary
  @Bean
  public VSAwareClinicalEventRepository clinicalEventRepository(
      ClinicalEventRepository clinicalEventRepository,
      VSAwarePatientRepository vsAwarePatientRepository) {
    return new VSAwareClinicalEventRepository(
        virtualizationService, clinicalEventRepository, vsAwarePatientRepository);
  }

  @Primary
  @Bean
  public VSAwareGenePanelRepository genePanelRepository(
      GenePanelRepository genePanelRepository, VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareGenePanelRepository(
        virtualizationService, genePanelRepository, sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareSampleListRepository sampleListRepository(
      SampleListRepository sampleListRepository) {
    return new VSAwareSampleListRepository(virtualizationService, sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareDiscreteCopyNumberRepository discreteCopyNumberRepository(
      DiscreteCopyNumberRepository discreteCopyNumberRepository,
      VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareDiscreteCopyNumberRepository(
        virtualizationService, discreteCopyNumberRepository, sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareMutationRepository mutationRepository(
      MutationRepository mutationRepository, VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareMutationRepository(
        virtualizationService, mutationRepository, sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareStructuralVariantRepository vsStructuralVariantRepository(
      StructuralVariantRepository structuralVariantRepository) {
    return new VSAwareStructuralVariantRepository(
        virtualizationService, structuralVariantRepository);
  }

  @Primary
  @Bean
  public VSAwareCopyNumberSegmentRepository vsAwareCopyNumberSegmentRepository(
      CopyNumberSegmentRepository copyNumberSegmentRepository,
      VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareCopyNumberSegmentRepository(
        virtualizationService, copyNumberSegmentRepository, sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareMolecularDataRepository molecularDataRepository(
      MolecularDataRepository molecularDataRepository) {
    return new VSAwareMolecularDataRepository(virtualizationService, molecularDataRepository);
  }

  @Primary
  @Bean
  public VSAwareVariantCountRepository variantCountRepository(
      VariantCountRepository variantCountRepository) {
    return new VSAwareVariantCountRepository(virtualizationService, variantCountRepository);
  }

  @Primary
  @Bean
  public VSAwareResourceDefinitionRepository resourceDefinitionRepository(
      ResourceDefinitionRepository resourceDefinitionRepository) {
    return new VSAwareResourceDefinitionRepository(
        virtualizationService, resourceDefinitionRepository);
  }

  @Primary
  @Bean
  public VSAwareResourceDataRepository resourceDataRepository(
      ResourceDataRepository resourceDataRepository) {
    return new VSAwareResourceDataRepository(virtualizationService, resourceDataRepository);
  }
}
