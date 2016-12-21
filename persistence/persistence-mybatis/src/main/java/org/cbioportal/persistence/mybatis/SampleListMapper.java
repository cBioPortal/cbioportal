package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.SampleList;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface SampleListMapper {
    
    List<SampleList> getAllSampleLists(@Param("studyId") String studyId,
                                       @Param("projection") String projection,
                                       @Param("limit") Integer limit,
                                       @Param("offset") Integer offset,
                                       @Param("sortBy") String sortBy,
                                       @Param("direction") String direction);


    BaseMeta getMetaSampleLists(@Param("studyId") String studyId);

    SampleList getSampleList(@Param("sampleListId") String sampleListId,
                             @Param("projection") String projection);

    List<String> getAllSampleIdsInSampleList(@Param("sampleListId") String sampleListId);
}
