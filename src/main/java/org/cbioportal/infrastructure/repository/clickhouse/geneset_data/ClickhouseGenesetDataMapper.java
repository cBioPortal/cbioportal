package org.cbioportal.infrastructure.repository.clickhouse.geneset_data;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.GenesetMolecularData;

public interface ClickhouseGenesetDataMapper {

  List<GenesetMolecularData> getGenesetData(
      @Param("molecularProfileId") String molecularProfileId,
      @Param("sampleIds") List<String> sampleIds,
      @Param("genesetIds") List<String> genesetIds);
}
