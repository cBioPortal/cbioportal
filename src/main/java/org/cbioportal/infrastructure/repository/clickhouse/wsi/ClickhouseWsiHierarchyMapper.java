package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import org.apache.ibatis.annotations.Param;

/** MyBatis access to the materialized WSI hierarchy table. */
public interface ClickhouseWsiHierarchyMapper {

  String getPatientHierarchy(
      @Param("studyId") String studyId, @Param("patientId") String patientId);
}
