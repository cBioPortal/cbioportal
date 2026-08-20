package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** MyBatis access to the normalized, release-versioned WSI tables. */
public interface ClickhouseWsiHierarchyMapper {

  List<Map<String, Object>> getPatientHierarchy(
      @Param("studyInternalId") long studyInternalId,
      @Param("releaseId") String releaseId,
      @Param("patientInternalId") long patientInternalId);
}
