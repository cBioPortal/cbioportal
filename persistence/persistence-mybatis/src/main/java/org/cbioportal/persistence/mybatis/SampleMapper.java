package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface SampleMapper {

    List<Sample> getSamples(@Param("studyIds") List<String> studyIds,
                            @Param("patientId") String patientId,
                            @Param("sampleIds") List<String> sampleIds,
                            @Param("projection") String projection,
                            @Param("limit") Integer limit,
                            @Param("offset") Integer offset,
                            @Param("sortBy") String sortBy,
                            @Param("direction") String direction);

    List<Sample> getSamplesInSameStudyByInternalIds(@Param("studyId") String studyId,
            @Param("patientId") String patientId,
            @Param("sampleIds") List<Integer> sampleInternalIds,
            @Param("projection") String projection,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset,
            @Param("sortBy") String sortBy,
            @Param("direction") String direction);
    
    BaseMeta getMetaSamples(@Param("studyIds") List<String> studyIds,
                            @Param("patientId") String patientId,
                            @Param("sampleIds") List<String> sampleIds);

    Sample getSample(@Param("studyId") String studyId,
                     @Param("sampleId") String sampleId,
                     @Param("projection") String projection);
}
