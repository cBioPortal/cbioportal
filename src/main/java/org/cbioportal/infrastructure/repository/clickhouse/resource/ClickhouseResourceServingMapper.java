package org.cbioportal.infrastructure.repository.clickhouse.resource;

import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** MyBatis lookup of one resource row's serving data. */
public interface ClickhouseResourceServingMapper {

  Map<String, Object> getServing(
      @Param("studyId") String studyId,
      @Param("resourceId") String resourceId,
      @Param("key") String key,
      @Param("patientId") String patientId);
}
