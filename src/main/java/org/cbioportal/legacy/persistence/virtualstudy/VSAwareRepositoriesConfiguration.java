package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.function.Predicate;
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
import org.cbioportal.legacy.service.CancerTypeService;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(
    name = "feature.published_virtual_study.single-sourced.backend-mode",
    havingValue = "true")
public class VSAwareRepositoriesConfiguration {

  @Autowired VirtualStudyService virtualStudyService;

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

  @Primary
  @Bean
  public VSAwareStudyRepository studyRepository(
      StudyRepository studyRepository, CancerTypeService cancerTypeService) {
    return new VSAwareStudyRepository(
        singleSourcedPublishedVirtualStudiesService(), studyRepository, cancerTypeService);
  }

  @Primary
  @Bean
  VSAwareClinicalAttributeRepository clinicalAttributeRepository(
      ClinicalAttributeRepository clinicalAttributeRepository) {
    return new VSAwareClinicalAttributeRepository(
        singleSourcedPublishedVirtualStudiesService(), clinicalAttributeRepository);
  }

  @Primary
  @Bean
  public VSAwareClinicalDataRepository clinicalDataRepository(
      ClinicalDataRepository clinicalDataRepository,
      VSAwarePatientRepository vsAwarePatientRepository) {
    return new VSAwareClinicalDataRepository(
        singleSourcedPublishedVirtualStudiesService(),
        clinicalDataRepository,
        vsAwarePatientRepository);
  }

  @Primary
  @Bean
  public VSAwareMolecularProfileRepository molecularProfileRepository(
      MolecularProfileRepository molecularProfileRepository) {
    return new VSAwareMolecularProfileRepository(
        singleSourcedPublishedVirtualStudiesService(), molecularProfileRepository);
  }

  @Primary
  @Bean
  public VSAwareSampleRepository sampleRepository(SampleRepository sampleRepository) {
    return new VSAwareSampleRepository(
        singleSourcedPublishedVirtualStudiesService(), sampleRepository);
  }

  @Primary
  @Bean
  public VSAwareGenericAssayRepository genericAssayRepository(
      GenericAssayRepository genericAssayRepository) {
    return new VSAwareGenericAssayRepository(
        singleSourcedPublishedVirtualStudiesService(), genericAssayRepository);
  }

  @Primary
  @Bean
  public VSAwareAlterationRepository alterationRepository(
      AlterationRepository alterationRepository) {
    return new VSAwareAlterationRepository(
        singleSourcedPublishedVirtualStudiesService(), alterationRepository);
  }

  @Primary
  @Bean
  public VSAwarePatientRepository patientRepository(PatientRepository patientRepository) {
    return new VSAwarePatientRepository(
        singleSourcedPublishedVirtualStudiesService(), patientRepository);
  }

  @Primary
  @Bean
  public VSAwareClinicalEventRepository clinicalEventRepository(
      ClinicalEventRepository clinicalEventRepository,
      VSAwarePatientRepository vsAwarePatientRepository) {
    return new VSAwareClinicalEventRepository(
        singleSourcedPublishedVirtualStudiesService(),
        clinicalEventRepository,
        vsAwarePatientRepository);
  }

  @Primary
  @Bean
  public VSAwareGenePanelRepository genePanelRepository(
      GenePanelRepository genePanelRepository,
      VSAwareMolecularProfileRepository molecularProfileRepository,
      VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareGenePanelRepository(
        singleSourcedPublishedVirtualStudiesService(),
        genePanelRepository,
        molecularProfileRepository,
        sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareSampleListRepository sampleListRepository(
      SampleListRepository sampleListRepository) {
    return new VSAwareSampleListRepository(
        singleSourcedPublishedVirtualStudiesService(), sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareDiscreteCopyNumberRepository discreteCopyNumberRepository(
      DiscreteCopyNumberRepository discreteCopyNumberRepository,
      VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareDiscreteCopyNumberRepository(
        singleSourcedPublishedVirtualStudiesService(),
        discreteCopyNumberRepository,
        sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareMutationRepository mutationRepository(
      org.cbioportal.legacy.persistence.MutationRepository mutationRepository,
      VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareMutationRepository(
        singleSourcedPublishedVirtualStudiesService(), mutationRepository, sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareStructuralVariantRepository vsStructuralVariantRepository(
      org.cbioportal.legacy.persistence.StructuralVariantRepository structuralVariantRepository) {
    return new VSAwareStructuralVariantRepository(
        singleSourcedPublishedVirtualStudiesService(), structuralVariantRepository);
  }

  @Primary
  @Bean
  public VSAwareCopyNumberSegmentRepository vsAwareCopyNumberSegmentRepository(
      org.cbioportal.legacy.persistence.CopyNumberSegmentRepository copyNumberSegmentRepository,
      VSAwareSampleListRepository sampleListRepository) {
    return new VSAwareCopyNumberSegmentRepository(
        singleSourcedPublishedVirtualStudiesService(),
        copyNumberSegmentRepository,
        sampleListRepository);
  }

  @Primary
  @Bean
  public VSAwareMolecularDataRepository molecularDataRepository(
      org.cbioportal.legacy.persistence.MolecularDataRepository molecularDataRepository,
      SampleRepository sampleRepository) {
    return new VSAwareMolecularDataRepository(
        singleSourcedPublishedVirtualStudiesService(), molecularDataRepository, sampleRepository);
  }

  @Primary
  @Bean
  public VSAwareVariantCountRepository variantCountRepository(
      org.cbioportal.legacy.persistence.VariantCountRepository variantCountRepository) {
    return new VSAwareVariantCountRepository(
        singleSourcedPublishedVirtualStudiesService(), variantCountRepository);
  }
}
