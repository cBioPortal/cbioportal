package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** MyBatis lookup of one slide row's tile-capability inputs. */
public interface ClickhouseWsiSlideAccessMapper {

  Map<String, Object> getSlideAccess(
      @Param("studyId") String studyId,
      @Param("patientId") String patientId,
      @Param("slideKey") String slideKey);
}
