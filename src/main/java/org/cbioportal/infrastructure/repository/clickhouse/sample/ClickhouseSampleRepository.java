package org.cbioportal.infrastructure.repository.clickhouse.sample;

import org.cbioportal.domain.sample.Sample;
import org.cbioportal.domain.sample.repository.SampleRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("clickhouse")
public class ClickhouseSampleRepository implements SampleRepository {

    private final ClickhouseSampleMapper mapper;

    public ClickhouseSampleRepository(ClickhouseSampleMapper clickhouseSampleMapper) {
        this.mapper = clickhouseSampleMapper;
    }

    /**
     * @param studyViewFilterContext
     * @return
     */
    @Override
    public List<Sample> getFilteredSamples(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getFilteredSamples(studyViewFilterContext);
    }

    /**
     * @param studyViewFilterContext
     * @return
     */
    @Override
    public int getFilteredSamplesCount(StudyViewFilterContext studyViewFilterContext) {
        return mapper.getSampleCount(studyViewFilterContext);
    }
}
