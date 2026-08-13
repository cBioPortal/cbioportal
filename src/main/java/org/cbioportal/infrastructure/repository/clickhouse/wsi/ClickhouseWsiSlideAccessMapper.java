package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** MyBatis access to active release slide pixel metadata. */
public interface ClickhouseWsiSlideAccessMapper {

  Map<String, Object> getSlideAccess(
      @Param("studyId") String studyId, @Param("imageId") String imageId);
}
