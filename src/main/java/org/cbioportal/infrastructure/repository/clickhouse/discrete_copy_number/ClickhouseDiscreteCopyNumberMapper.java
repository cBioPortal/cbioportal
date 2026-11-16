package org.cbioportal.infrastructure.repository.clickhouse.discrete_copy_number;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.DiscreteCopyNumberData;

public interface ClickhouseDiscreteCopyNumberMapper {

  List<DiscreteCopyNumberData> getDiscreteCopyNumbers(
      @Param("molecularProfileId") String molecularProfileId,
      @Param("sampleListId") String sampleListId,
      @Param("sampleIds") List<String> sampleIds,
      @Param("entrezGeneIds") List<Integer> entrezGeneIds,
      @Param("alterationTypes") List<Integer> alterationTypes,
      @Param("includeGene") boolean includeGene);

  int countDiscreteCopyNumbers(
      @Param("molecularProfileId") String molecularProfileId,
      @Param("sampleListId") String sampleListId,
      @Param("sampleIds") List<String> sampleIds,
      @Param("entrezGeneIds") List<Integer> entrezGeneIds,
      @Param("alterationTypes") List<Integer> alterationTypes);
}
