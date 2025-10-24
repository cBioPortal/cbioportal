package org.cbioportal.application.file.services;

import java.util.Set;
import org.cbioportal.application.file.repositories.SVRepository;
import org.cbioportal.application.file.model.StructuralVariant;
import org.cbioportal.application.file.utils.CloseableIterator;

public class StructuralVariantService {

  private final SVRepository svRepository;

  public StructuralVariantService(SVRepository svRepository) {
    this.svRepository = svRepository;
  }

  public CloseableIterator<StructuralVariant> getStructuralVariants(
      String geneticDatatypeStableId, Set<String> sampleIds) {
    return svRepository.getStructuralVariants(geneticDatatypeStableId, sampleIds);
  }
}
