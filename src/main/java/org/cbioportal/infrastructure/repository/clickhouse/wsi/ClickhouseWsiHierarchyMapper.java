package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** MyBatis access to the WSI_SAMPLE/WSI_PATIENT rows of resource_data. */
public interface ClickhouseWsiHierarchyMapper {

  List<Map<String, Object>> getPatientHierarchy(
      @Param("studyInternalId") long studyInternalId, @Param("patientId") String patientId);
}
