package org.cbioportal.domain.sample.usecase;

import java.util.List;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.shared.util.SampleDataFilterUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class FetchMetaSamplesUseCase {
  private final SampleRepository sampleRepository;

  public FetchMetaSamplesUseCase(SampleRepository sampleRepository) {
    this.sampleRepository = sampleRepository;
  }

  public BaseMeta execute(SampleFilter sampleFilter) {
    BaseMeta baseMeta;

    if (sampleFilter.getSampleListIds() != null) {
      baseMeta = sampleRepository.fetchMetaSamplesBySampleListIds(sampleFilter.getSampleListIds());
    } else {
      Pair<List<String>, List<String>> studyAndSampleIds =
          SampleDataFilterUtil.extractStudyAndSampleIds(sampleFilter);
      baseMeta =
          sampleRepository.fetchMetaSamples(
              studyAndSampleIds.getFirst(), studyAndSampleIds.getSecond());
    }

    return baseMeta;
  }
}
