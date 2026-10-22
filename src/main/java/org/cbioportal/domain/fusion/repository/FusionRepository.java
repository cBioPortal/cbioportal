package org.cbioportal.domain.fusion.repository;

import java.util.List;
import org.cbioportal.domain.fusion.FusionFilter;
import org.cbioportal.legacy.model.StructuralVariant;

/** Repository for fusions. */
public interface FusionRepository {

  /**
   * Fetches the fusions matching the filter. Results are restricted to the studies in {@link
   * FusionFilter#studyIds()}.
   *
   * <p>There is no dedicated fusion table yet, so fusions are structural variants that satisfy a
   * fusion criterion.
   */
  List<StructuralVariant> fetchFusions(FusionFilter filter);
}
