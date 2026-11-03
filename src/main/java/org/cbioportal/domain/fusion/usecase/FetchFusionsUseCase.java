package org.cbioportal.domain.fusion.usecase;

import java.util.List;
import org.cbioportal.domain.fusion.FusionFilter;
import org.cbioportal.domain.fusion.repository.FusionRepository;
import org.cbioportal.legacy.model.StructuralVariant;
import org.springframework.stereotype.Service;

/** Use case for retrieving fusions. */
@Service
public class FetchFusionsUseCase {
  private final FusionRepository fusionRepository;

  public FetchFusionsUseCase(FusionRepository fusionRepository) {
    this.fusionRepository = fusionRepository;
  }

  public List<StructuralVariant> execute(FusionFilter filter) {
    return fusionRepository.fetchFusions(filter);
  }
}
