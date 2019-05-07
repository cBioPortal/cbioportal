package org.cbioportal.persistence;

import org.cbioportal.model.SampleList;
import org.cbioportal.model.SampleListToSampleId;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface SampleListRepository {

    @Cacheable("RepositoryCache")
    List<SampleList> getAllSampleLists(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                       String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaSampleLists();

    @Cacheable("RepositoryCache")
    SampleList getSampleList(String sampleListId);

    @Cacheable("RepositoryCache")
    List<SampleList> getSampleLists(List<String> sampleListIds, String projection);

    @Cacheable("RepositoryCache")
    List<SampleList> getAllSampleListsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                              String sortBy, String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaSampleListsInStudy(String studyId);

    @Cacheable("RepositoryCache")
    List<String> getAllSampleIdsInSampleList(String sampleListId);

    @Cacheable("RepositoryCache")
    List<SampleListToSampleId> getSampleListSampleIds(List<Integer> sampleListIds);
}
