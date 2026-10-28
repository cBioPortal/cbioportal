package org.cbioportal.domain.ascn.usecase;

import java.util.List;
import org.cbioportal.domain.ascn.repository.AscnSegmentRepository;
import org.cbioportal.domain.ascn.util.AscnSegmentUtil;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.springframework.stereotype.Service;

@Service
public class FetchMetaAscnSegmentsUseCase {
  private final AscnSegmentRepository ascnSegmentRepository;

  public FetchMetaAscnSegmentsUseCase(AscnSegmentRepository ascnSegmentRepository) {
    this.ascnSegmentRepository = ascnSegmentRepository;
  }

  public BaseMeta execute(List<SampleIdentifier> sampleIdentifiers) {
    List<String> studyIds = AscnSegmentUtil.extractStudyIds(sampleIdentifiers);
    List<String> sampleIds = AscnSegmentUtil.extractSampleIds(sampleIdentifiers);
    return ascnSegmentRepository.getMetaAscnSegments(studyIds, sampleIds);
  }
}
