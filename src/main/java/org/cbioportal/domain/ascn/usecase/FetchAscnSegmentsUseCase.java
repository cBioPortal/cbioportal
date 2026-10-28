package org.cbioportal.domain.ascn.usecase;

import java.util.List;
import org.cbioportal.domain.ascn.AscnSegment;
import org.cbioportal.domain.ascn.repository.AscnSegmentRepository;
import org.cbioportal.domain.ascn.util.AscnSegmentUtil;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.springframework.stereotype.Service;

@Service
public class FetchAscnSegmentsUseCase {
  private final AscnSegmentRepository ascnSegmentRepository;

  public FetchAscnSegmentsUseCase(AscnSegmentRepository ascnSegmentRepository) {
    this.ascnSegmentRepository = ascnSegmentRepository;
  }

  public List<AscnSegment> execute(List<SampleIdentifier> sampleIdentifiers) {
    List<String> studyIds = AscnSegmentUtil.extractStudyIds(sampleIdentifiers);
    List<String> sampleIds = AscnSegmentUtil.extractSampleIds(sampleIdentifiers);
    return ascnSegmentRepository.getAscnSegments(studyIds, sampleIds, null, null);
  }
}
