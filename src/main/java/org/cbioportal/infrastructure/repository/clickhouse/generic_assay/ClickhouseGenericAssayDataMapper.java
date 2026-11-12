package org.cbioportal.infrastructure.repository.clickhouse.generic_assay;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.legacy.model.GenericAssayData;

public interface ClickhouseGenericAssayDataMapper {

  List<GenericAssayData> getGenericAssayData(
      @Param("studyId") String studyId,
      @Param("molecularProfileId") String molecularProfileId,
      @Param("profileType") String profileType,
      @Param("sampleUniqueIds") String[] sampleUniqueIds,
      @Param("genericAssayStableIds") List<String> genericAssayStableIds);
}
