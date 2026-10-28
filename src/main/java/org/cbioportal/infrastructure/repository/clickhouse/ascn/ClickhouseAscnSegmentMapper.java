package org.cbioportal.infrastructure.repository.clickhouse.ascn;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.domain.ascn.AscnSegment;
import org.cbioportal.legacy.model.meta.BaseMeta;

public interface ClickhouseAscnSegmentMapper {

  List<AscnSegment> getAscnSegments(
      @Param("studyIds") List<String> studyIds,
      @Param("sampleIds") List<String> sampleIds,
      @Param("limit") Integer limit,
      @Param("offset") Integer offset);

  BaseMeta getMetaAscnSegments(
      @Param("studyIds") List<String> studyIds, @Param("sampleIds") List<String> sampleIds);
}
