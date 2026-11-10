package org.cbioportal.infrastructure.repository.clickhouse.structural_variant;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.model.StructuralVariantQuery;

public interface ClickhouseStructuralVariantMapper {

  List<StructuralVariant> fetchStructuralVariants(
      @Param("molecularProfileIds") List<String> molecularProfileIds,
      @Param("singleProfileId") String singleProfileId,
      @Param("sampleIds") String[] sampleIds,
      @Param("sampleProfileKeys") String[] sampleProfileKeys,
      @Param("entrezGeneIds") List<Integer> entrezGeneIds,
      @Param("structuralVariantQueries") List<StructuralVariantQuery> structuralVariantQueries);
}
