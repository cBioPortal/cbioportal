package org.cbioportal.domain.sample.usecase;

import java.util.List;
import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.legacy.web.parameter.SampleFilter;
import org.cbioportal.shared.enums.ProjectionType;
import org.cbioportal.shared.util.SampleDataFilterUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class FetchSamplesUseCase {
  private final SampleRepository sampleRepository;

  public FetchSamplesUseCase(SampleRepository sampleRepository) {
    this.sampleRepository = sampleRepository;
  }

  public List<Sample> execute(SampleFilter sampleFilter, ProjectionType projection) {
    List<Sample> samples;

    if (sampleFilter.getSampleListIds() != null) {
      List<String> sampleListIds = sampleFilter.getSampleListIds();
      samples = sampleRepository.fetchSamplesBySampleListIds(sampleListIds, projection);
    } else {
      Pair<List<String>, List<String>> studyAndSampleIds =
          SampleDataFilterUtil.extractStudyAndSampleIds(sampleFilter);
      samples =
          sampleRepository.fetchSamples(
              studyAndSampleIds.getFirst(), studyAndSampleIds.getSecond(), projection);
    }

    return samples;
  }
}
