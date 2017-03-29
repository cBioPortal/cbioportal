package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface SampleMapper {

    List<Sample> getSamples(List<String> studyIds, String patientId, List<String> sampleIds, String projection, 
                            Integer limit, Integer offset, String sortBy, String direction);

    BaseMeta getMetaSamples(List<String> studyIds, String patientId, List<String> sampleIds);

    Sample getSample(String studyId, String sampleId, String projection);

    List<Sample> getSamplesByInternalIds(List<Integer> internalIds, String projection);
}
