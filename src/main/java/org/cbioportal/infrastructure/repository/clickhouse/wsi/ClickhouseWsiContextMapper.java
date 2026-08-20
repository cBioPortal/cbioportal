package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** Resolves the small set of portal identifiers needed to prune WSI tables. */
public interface ClickhouseWsiContextMapper {

  Map<String, Object> getStudyContext(@Param("studyId") String studyId);

  Map<String, Object> getPatientContext(
      @Param("studyId") String studyId, @Param("patientId") String patientId);
}
