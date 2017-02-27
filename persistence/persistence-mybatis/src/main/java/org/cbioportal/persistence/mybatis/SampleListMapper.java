package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.SampleList;
import org.cbioportal.model.SampleListSampleCount;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface SampleListMapper {
    
    List<SampleList> getAllSampleLists(String studyId, String projection, Integer limit, Integer offset, String sortBy, 
                                       String direction);
    
    BaseMeta getMetaSampleLists(String studyId);

    SampleList getSampleList(String sampleListId, String projection);

    List<String> getAllSampleIdsInSampleList(String sampleListId);
    
    List<SampleListSampleCount> getSampleCounts(List<Integer> sampleListIds);
}
