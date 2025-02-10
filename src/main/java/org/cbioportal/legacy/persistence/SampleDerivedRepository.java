package org.cbioportal.legacy.persistence;

import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;

import java.util.List;

public interface SampleDerivedRepository {
    List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection);

    List<Sample> fetchSamplesBySampleListIds(List<String> sampleListIds, String projection);

    BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds);
    
    BaseMeta fetchMetaSamplesBySampleListIds(List<String> sampleListIds);
}
