package org.cbioportal.domain.sample.usecase;

import java.util.List;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class GetMetaSamplesUseCase {
  private final SampleRepository sampleRepository;

  public GetMetaSamplesUseCase(SampleRepository sampleRepository) {
    this.sampleRepository = sampleRepository;
  }

  public BaseMeta execute(String keyword, List<String> studyIds) {
    return sampleRepository.getMetaSamples(keyword, studyIds);
  }
}
