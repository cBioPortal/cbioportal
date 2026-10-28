package org.cbioportal.infrastructure.repository.clickhouse.ascn;

import java.util.List;
import org.cbioportal.domain.ascn.AscnSegment;
import org.cbioportal.domain.ascn.repository.AscnSegmentRepository;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.springframework.stereotype.Repository;

@Repository
public class ClickhouseAscnSegmentRepository implements AscnSegmentRepository {

  private final ClickhouseAscnSegmentMapper mapper;

  public ClickhouseAscnSegmentRepository(ClickhouseAscnSegmentMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<AscnSegment> getAscnSegments(
      List<String> studyIds, List<String> sampleIds, Integer limit, Integer offset) {
    return mapper.getAscnSegments(studyIds, sampleIds, limit, offset);
  }

  @Override
  public BaseMeta getMetaAscnSegments(List<String> studyIds, List<String> sampleIds) {
    return mapper.getMetaAscnSegments(studyIds, sampleIds);
  }
}
