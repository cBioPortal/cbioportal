package org.cbioportal.infrastructure.repository.clickhouse.structural_variant;

import java.util.List;
import java.util.stream.IntStream;
import org.cbioportal.domain.structural_variant.repository.StructuralVariantRepository;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.model.StructuralVariantQuery;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseStructuralVariantRepository implements StructuralVariantRepository {

  private final ClickhouseStructuralVariantMapper mapper;

  public ClickhouseStructuralVariantRepository(ClickhouseStructuralVariantMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<StructuralVariant> fetchStructuralVariants(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<StructuralVariantQuery> structuralVariantQueries) {

    String singleProfileId = null;
    String[] sampleArray = null;
    String[] sampleProfileKeys = null;
    if (sampleIds != null && !sampleIds.isEmpty()) {
      if (molecularProfileIds.stream().distinct().count() == 1) {
        singleProfileId = molecularProfileIds.get(0);
        sampleArray = sampleIds.toArray(new String[0]);
      } else {
        // several profiles: match exact (sample, profile) pairs rather than the cross product
        sampleProfileKeys =
            IntStream.range(0, sampleIds.size())
                .mapToObj(i -> sampleIds.get(i) + ":" + molecularProfileIds.get(i))
                .toArray(String[]::new);
      }
    }
    return mapper.fetchStructuralVariants(
        molecularProfileIds,
        singleProfileId,
        sampleArray,
        sampleProfileKeys,
        entrezGeneIds,
        structuralVariantQueries);
  }
}
