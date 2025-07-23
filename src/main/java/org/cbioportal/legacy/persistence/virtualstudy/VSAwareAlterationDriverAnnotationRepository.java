package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.cbioportal.legacy.model.AlterationDriverAnnotation;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.AlterationDriverAnnotationRepository;

public class VSAwareAlterationDriverAnnotationRepository
    implements AlterationDriverAnnotationRepository {

  private final VirtualizationService virtualizationService;
  private final AlterationDriverAnnotationRepository alterationDriverAnnotationRepository;

  public VSAwareAlterationDriverAnnotationRepository(
      VirtualizationService virtualizationService,
      AlterationDriverAnnotationRepository alterationDriverAnnotationRepository) {
    this.virtualizationService = virtualizationService;
    this.alterationDriverAnnotationRepository = alterationDriverAnnotationRepository;
  }

  @Override
  public List<AlterationDriverAnnotation> getAlterationDriverAnnotations(
      List<String> molecularProfileCaseIdentifiers) {
    return virtualizationService.handleMolecularData(
        new HashSet<>(molecularProfileCaseIdentifiers),
        AlterationDriverAnnotation::getGeneticProfileId,
        mpids ->
            alterationDriverAnnotationRepository.getAlterationDriverAnnotations(
                new ArrayList<>(mpids)),
        this::virtualizeAlterationDriverAnnotation);
  }

  private AlterationDriverAnnotation virtualizeAlterationDriverAnnotation(
      MolecularProfile molecularProfile, AlterationDriverAnnotation alterationDriverAnnotation) {
    AlterationDriverAnnotation virtualizedAnnotation = new AlterationDriverAnnotation();
    virtualizedAnnotation.setGeneticProfileId(molecularProfile.getStableId());
    virtualizedAnnotation.setDriverFilter(alterationDriverAnnotation.getDriverFilter());
    virtualizedAnnotation.setDriverFilterAnnotation(
        alterationDriverAnnotation.getDriverFilterAnnotation());
    virtualizedAnnotation.setDriverTiersFilter(alterationDriverAnnotation.getDriverTiersFilter());
    virtualizedAnnotation.setDriverTiersFilterAnnotation(
        alterationDriverAnnotation.getDriverTiersFilterAnnotation());
    return virtualizedAnnotation;
  }
}
