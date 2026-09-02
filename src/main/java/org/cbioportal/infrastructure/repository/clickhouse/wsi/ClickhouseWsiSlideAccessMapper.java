package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import java.util.Map;
import org.apache.ibatis.annotations.Param;

/** MyBatis access to slide pixel metadata. */
public interface ClickhouseWsiSlideAccessMapper {

  Map<String, Object> getSlideAccess(
      @Param("studyInternalId") long studyInternalId,
      @Param("imageId") String imageId);
}
