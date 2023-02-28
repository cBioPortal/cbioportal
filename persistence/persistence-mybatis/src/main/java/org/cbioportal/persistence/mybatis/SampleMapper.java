package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface SampleMapper {

    List<Sample> getSamples(List<String> studyIds, String patientId, List<String> sampleIds, String keyword, 
                            String projection, Integer limit, Integer offset, String sortBy, String direction);

    List<Sample> getSamplesBySampleListIds(List<String> sampleListIds, String projection);

    BaseMeta getMetaSamples(List<String> studyIds, String patientId, List<String> sampleIds, String keyword);

    BaseMeta getMetaSamplesBySampleListIds(List<String> sampleListIds);

    Sample getSample(String studyId, String sampleId, String projection);

    List<Sample> getSamplesByInternalIds(List<Integer> internalIds, String projection);

    List<Sample> getSamplesOfPatients(String studyId, List<String> patientIds, String projection);

    List<Sample> getSamplesOfPatientsInMultipleStudies(List<String> studyIds, List<String> patientIds, String projection);
}
