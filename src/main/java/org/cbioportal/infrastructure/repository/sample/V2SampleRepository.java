package org.cbioportal.infrastructure.repository.sample;

import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.SampleList;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class V2SampleRepository implements org.cbioportal.domain.sample.repository.SampleRepository {

    private final V2SampleMapper mapper;

    public V2SampleRepository(V2SampleMapper sampleMapper) {
        this.mapper = sampleMapper;
    }

    @Override
    public List<Sample> getFilteredSamples(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getFilteredSamples(studyViewFilterContext);
    }

    @Override
    public int getFilteredSamplesCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getSampleCount(studyViewFilterContext);
    }

    @Override
    public List<SampleList> getSampleLists(List<String> cancerStudyIds) {
        return mapper.getSampleLists(cancerStudyIds);
    }
}
