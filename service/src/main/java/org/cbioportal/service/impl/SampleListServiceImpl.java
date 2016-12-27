package org.cbioportal.service.impl;

import org.cbioportal.model.SampleList;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SampleListServiceImpl implements SampleListService {

    @Autowired
    private SampleListRepository sampleListRepository;

    @Override
    public List<SampleList> getAllSampleLists(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                              String direction) {

        return sampleListRepository.getAllSampleLists(projection, pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSampleLists() {

        return sampleListRepository.getMetaSampleLists();
    }

    @Override
    public SampleList getSampleList(String sampleListId) throws SampleListNotFoundException {

        SampleList sampleList = sampleListRepository.getSampleList(sampleListId);
        if (sampleList == null) {
            throw new SampleListNotFoundException(sampleListId);
        }

        return sampleList;
    }

    @Override
    public List<SampleList> getAllSampleListsInStudy(String studyId, String projection, Integer pageSize,
                                                     Integer pageNumber, String sortBy, String direction) {

        return sampleListRepository.getAllSampleListsInStudy(studyId, projection, pageSize, pageNumber, sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSampleListsInStudy(String studyId) {

        return sampleListRepository.getMetaSampleListsInStudy(studyId);
    }

    @Override
    public List<String> getAllSampleIdsInSampleList(String sampleListId) {

        return sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
    }
}
