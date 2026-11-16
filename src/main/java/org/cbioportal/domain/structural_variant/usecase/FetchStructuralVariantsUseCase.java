package org.cbioportal.domain.structural_variant.usecase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.structural_variant.repository.StructuralVariantRepository;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.legacy.web.parameter.StructuralVariantFilter;
import org.springframework.stereotype.Service;

/**
 * Fetches structural variants for the profiles or (profile, sample) pairs of a filter. The pairs
 * take precedence whenever they are present.
 */
@Service
public class FetchStructuralVariantsUseCase {

  private final StructuralVariantRepository structuralVariantRepository;

  public FetchStructuralVariantsUseCase(StructuralVariantRepository structuralVariantRepository) {
    this.structuralVariantRepository = structuralVariantRepository;
  }

  public List<StructuralVariant> execute(StructuralVariantFilter filter) {
    List<String> molecularProfileIds = new ArrayList<>();
    List<String> sampleIds = null;

    if (filter.getSampleMolecularIdentifiers() != null) {
      sampleIds = new ArrayList<>();
      for (SampleMolecularIdentifier identifier : filter.getSampleMolecularIdentifiers()) {
        molecularProfileIds.add(identifier.getMolecularProfileId());
        sampleIds.add(identifier.getSampleId());
      }
    } else if (filter.getMolecularProfileIds() != null) {
      molecularProfileIds.addAll(filter.getMolecularProfileIds());
    }

    if (molecularProfileIds.isEmpty()) {
      return Collections.emptyList();
    }
    return structuralVariantRepository.fetchStructuralVariants(
        molecularProfileIds,
        sampleIds,
        filter.getEntrezGeneIds(),
        filter.getStructuralVariantQueries());
  }
}
