package org.cbioportal.persistence;

import org.cbioportal.model.SampleList;
import org.cbioportal.model.SampleListToSampleId;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface SampleListRepository {

    @Cacheable("GeneralRepositoryCache")
    List<SampleList> getAllSampleLists(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                       String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaSampleLists();

    @Cacheable("GeneralRepositoryCache")
    SampleList getSampleList(String sampleListId);

    @Cacheable("GeneralRepositoryCache")
    List<SampleList> getSampleLists(List<String> sampleListIds, String projection);

    @Cacheable("GeneralRepositoryCache")
    List<SampleList> getAllSampleListsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                              String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaSampleListsInStudy(String studyId);

    @Cacheable("GeneralRepositoryCache")
    List<String> getAllSampleIdsInSampleList(String sampleListId);

    @Cacheable("GeneralRepositoryCache")
    List<SampleListToSampleId> getSampleListSampleIds(List<Integer> sampleListIds);
}
