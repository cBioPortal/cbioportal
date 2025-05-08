package org.cbioportal.domain.sample.usecase;

import java.util.List;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class GetMetaSamplesUseCase {
  private final SampleRepository sampleRepository;

  public GetMetaSamplesUseCase(SampleRepository sampleRepository) {
    this.sampleRepository = sampleRepository;
  }

  public BaseMeta execute(String keyword, List<String> studyIds) {
    return sampleRepository.getMetaSamples(keyword, studyIds);
  }
}
