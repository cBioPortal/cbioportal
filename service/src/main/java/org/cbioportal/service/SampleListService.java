package org.cbioportal.service;

import org.cbioportal.model.SampleList;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface SampleListService {

    List<SampleList> getAllSampleLists(String projection, Integer pageSize, Integer pageNumber, String sortBy,
            String direction);

    BaseMeta getMetaSampleLists();

    SampleList getSampleList(String sampleListId) throws SampleListNotFoundException;

    List<SampleList> getAllSampleListsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
            String sortBy, String direction) throws StudyNotFoundException;

    BaseMeta getMetaSampleListsInStudy(String studyId) throws StudyNotFoundException;

    List<String> getAllSampleIdsInSampleList(String sampleListId) throws SampleListNotFoundException;

    List<SampleList> fetchSampleLists(List<String> sampleListIds, String projection);

    List<SampleList> getAllSampleListsInStudies(List<String> studyIds, String projection);
}
