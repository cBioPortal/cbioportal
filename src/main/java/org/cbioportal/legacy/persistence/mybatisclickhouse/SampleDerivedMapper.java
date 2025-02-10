package org.cbioportal.legacy.persistence.mybatisclickhouse;

import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.meta.BaseMeta;

import java.util.List;

public interface SampleDerivedMapper {
    BaseMeta getMetaSamples(List<String> studyIds, String patientId, List<String> sampleIds, String keyword);

    BaseMeta getMetaSamplesBySampleListIds(List<String> sampleListIds);

    List<Sample> getSamples(List<String> studyIds, String patientId, List<String> sampleIds, String keyword, String projection);
    
    List<Sample> getSamplesBySampleListIds(List<String> sampleListIds, String projection);
}
