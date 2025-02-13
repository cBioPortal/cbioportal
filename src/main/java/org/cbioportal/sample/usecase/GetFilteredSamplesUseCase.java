package org.cbioportal.sample.usecase;

import org.cbioportal.sample.Sample;
import org.cbioportal.sample.repository.SampleRepository;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public final class GetFilteredSamplesUseCase {

   private final SampleRepository sampleRepository;

   public GetFilteredSamplesUseCase(SampleRepository sampleRepository) {
       this.sampleRepository = sampleRepository;
   }

   public List<Sample> execute(StudyViewFilterContext studyViewFilterContext) {
       return this.sampleRepository.getFilteredSamples(studyViewFilterContext);
   }
}
