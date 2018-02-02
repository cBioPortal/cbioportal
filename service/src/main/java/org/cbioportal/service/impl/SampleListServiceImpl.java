package org.cbioportal.service.impl;

import org.cbioportal.model.SampleList;
import org.cbioportal.model.SampleListToSampleId;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SampleListServiceImpl implements SampleListService {

    @Autowired
    private SampleListRepository sampleListRepository;
    @Autowired
    private StudyService studyService;

    @Override
    @PostFilter("hasPermission(filterObject, 'read')")
    public List<SampleList> getAllSampleLists(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                              String direction) {

        List<SampleList> sampleLists = sampleListRepository.getAllSampleLists(projection, pageSize, pageNumber, sortBy,
            direction);
        
        if(projection.equals("DETAILED")) {
            addSampleIds(sampleLists);
            addSampleCounts(sampleLists);
        }
        
        return sampleLists;
    }

    @Override
    public BaseMeta getMetaSampleLists() {

        return sampleListRepository.getMetaSampleLists();
    }

    @Override
    @PreAuthorize("hasPermission(#sampleListId, 'SampleList', 'read')")
    public SampleList getSampleList(String sampleListId) throws SampleListNotFoundException {

        SampleList sampleList = sampleListRepository.getSampleList(sampleListId);
        if (sampleList == null) {
            throw new SampleListNotFoundException(sampleListId);
        }

        List<SampleListToSampleId> sampleListToSampleIds = sampleListRepository.getSampleListSampleIds(
            Arrays.asList(sampleList.getListId()));
        sampleList.setSampleIds(sampleListToSampleIds.stream().map(SampleListToSampleId::getSampleId)
        .collect(Collectors.toList()));
        sampleList.setSampleCount(sampleList.getSampleIds().size());
        return sampleList;
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public List<SampleList> getAllSampleListsInStudy(String studyId, String projection, Integer pageSize,
                                                     Integer pageNumber, String sortBy, String direction) 
        throws StudyNotFoundException {
        
        studyService.getStudy(studyId);

        List<SampleList> sampleLists = sampleListRepository.getAllSampleListsInStudy(studyId, projection, pageSize, 
            pageNumber, sortBy, direction);

        if(projection.equals("DETAILED")) {
            addSampleIds(sampleLists);
            addSampleCounts(sampleLists);
        }
        
        return sampleLists;
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public BaseMeta getMetaSampleListsInStudy(String studyId) throws StudyNotFoundException {

        studyService.getStudy(studyId);

        return sampleListRepository.getMetaSampleListsInStudy(studyId);
    }

    @Override
    @PreAuthorize("hasPermission(#sampleListId, 'SampleList', 'read')")
    public List<String> getAllSampleIdsInSampleList(String sampleListId) throws SampleListNotFoundException {
        
        getSampleList(sampleListId);

        return sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
    }

    @Override
    @PreAuthorize("hasPermission(#sampleListIds, 'List<SampleListId>', 'read')")
	public List<SampleList> fetchSampleLists(List<String> sampleListIds, String projection) {

        List<SampleList> sampleLists = sampleListRepository.getSampleLists(sampleListIds, projection);

        if (projection.equals("DETAILED")) {
            addSampleIds(sampleLists);
            addSampleCounts(sampleLists);
        }

        return sampleLists;
	}

    private void addSampleCounts(List<SampleList> sampleLists) {

        sampleLists.forEach(s -> s.setSampleCount(s.getSampleIds().size()));
    }

    private void addSampleIds(List<SampleList> sampleLists) {

        List<SampleListToSampleId> sampleListToSampleIds = sampleListRepository.getSampleListSampleIds(sampleLists
        .stream().map(SampleList::getListId).collect(Collectors.toList()));

        sampleLists.forEach(s -> s.setSampleIds(sampleListToSampleIds.stream().filter(p -> p.getSampleListId()
        .equals(s.getListId())).map(SampleListToSampleId::getSampleId).collect(Collectors.toList())));
    }
}
