package org.cbioportal.service;

import org.cbioportal.model.SampleList;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.SampleListNotFoundException;

import java.util.List;

public interface SampleListService {

    List<SampleList> getAllSampleLists(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                       String direction);


    BaseMeta getMetaSampleLists();

    SampleList getSampleList(String sampleListId) throws SampleListNotFoundException;

    List<SampleList> getAllSampleListsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                              String sortBy, String direction);

    BaseMeta getMetaSampleListsInStudy(String studyId);

    List<String> getAllSampleIdsInSampleList(String sampleListId);
}
