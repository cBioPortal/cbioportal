package org.cbioportal.legacy.persistence.mybatisclickhouse;

import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.SampleDerivedRepository;
import org.cbioportal.legacy.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
public class SampleDerivedMyBatisRepository implements SampleDerivedRepository {
    @Autowired
    private SampleDerivedMapper sampleMapper;
    
    @Override
    public List<Sample> fetchSamples(
        List<String> studyIds,
        List<String> sampleIds,
        String projection
    ) {
        return sampleMapper.getSamples(studyIds, null, sampleIds, null, projection);
    }

    @Override
    public List<Sample> fetchSamplesBySampleListIds(
        List<String> sampleListIds,
        String projection
    ) {
        return sampleMapper.getSamplesBySampleListIds(sampleListIds, projection);
    }

    @Override
    public BaseMeta fetchMetaSamples(
        List<String> studyIds,
        List<String> sampleIds
    ) {
        return sampleMapper.getMetaSamples(studyIds, null, sampleIds, null);
    }

    @Override
    public BaseMeta fetchMetaSamplesBySampleListIds(List<String> sampleListIds) {
        return sampleMapper.getMetaSamplesBySampleListIds(sampleListIds);
    }
}
