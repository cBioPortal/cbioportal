package org.cbioportal.infrastructure.repository.clickhouse.fusion;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.StructuralVariant;

public interface ClickhouseFusionMapper {

  List<StructuralVariant> getFusions(
      @Param("studyIds") List<String> studyIds,
      @Param("molecularProfileIds") List<String> molecularProfileIds,
      @Param("entrezGeneIds") List<Integer> entrezGeneIds);
}
