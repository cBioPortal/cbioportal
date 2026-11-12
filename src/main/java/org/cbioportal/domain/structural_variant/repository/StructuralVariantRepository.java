package org.cbioportal.domain.structural_variant.repository;

import java.util.List;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.model.StructuralVariantQuery;

public interface StructuralVariantRepository {

  /**
   * Fetches structural variants having an entrez gene id in {@code entrezGeneIds} on either side,
   * or matching one of the {@code structuralVariantQueries}. Without any of them all structural
   * variants of the selected profiles and samples are returned.
   *
   * @param molecularProfileIds profiles to read from
   * @param sampleIds null for all samples of the profiles. Otherwise a list parallel to {@code
   *     molecularProfileIds}: entry i is a sample selected in profile i
   */
  List<StructuralVariant> fetchStructuralVariants(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<StructuralVariantQuery> structuralVariantQueries);
}
