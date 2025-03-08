package org.cbioportal.domain.sample.usecase;

import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.SampleList;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class GetStudiesSampleListsUseCase {

   private final SampleRepository sampleRepository;

   public GetStudiesSampleListsUseCase(SampleRepository sampleRepository) {
       this.sampleRepository = sampleRepository;
   }

   public List<SampleList> execute(List<String> cancerStudyIds) {
       return this.sampleRepository.getSampleLists(cancerStudyIds);
   }
}
