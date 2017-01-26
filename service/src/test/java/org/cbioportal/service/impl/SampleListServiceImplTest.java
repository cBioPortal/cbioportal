package org.cbioportal.service.impl;

import org.cbioportal.model.SampleList;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SampleListServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private SampleListServiceImpl sampleListService;

    @Mock
    private SampleListRepository sampleListRepository;

    @Test
    public void getAllSampleLists() throws Exception {

        List<SampleList> expectedSampleLists = new ArrayList<>();
        SampleList sampleList = new SampleList();
        expectedSampleLists.add(sampleList);

        Mockito.when(sampleListRepository.getAllSampleLists(PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION)).thenReturn(expectedSampleLists);

        List<SampleList> result = sampleListService.getAllSampleLists(PROJECTION, PAGE_SIZE, PAGE_NUMBER,
            SORT, DIRECTION);

        Assert.assertEquals(expectedSampleLists, result);
    }

    @Test
    public void getMetaSampleLists() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(sampleListRepository.getMetaSampleLists()).thenReturn(expectedBaseMeta);

        BaseMeta result = sampleListService.getMetaSampleLists();

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = SampleListNotFoundException.class)
    public void getSampleListNotFound() throws Exception {

        Mockito.when(sampleListRepository.getSampleList(SAMPLE_LIST_ID)).thenReturn(null);

        sampleListService.getSampleList(SAMPLE_LIST_ID);
    }

    @Test
    public void getSampleList() throws Exception {

        SampleList expectedSampleList = new SampleList();

        Mockito.when(sampleListRepository.getSampleList(SAMPLE_LIST_ID)).thenReturn(expectedSampleList);

        SampleList result = sampleListService.getSampleList(SAMPLE_LIST_ID);

        Assert.assertEquals(expectedSampleList, result);
    }

    @Test
    public void getAllSampleListsInStudy() throws Exception {

        List<SampleList> expectedSampleLists = new ArrayList<>();
        SampleList sampleList = new SampleList();
        expectedSampleLists.add(sampleList);

        Mockito.when(sampleListRepository.getAllSampleListsInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER,
            SORT, DIRECTION)).thenReturn(expectedSampleLists);

        List<SampleList> result = sampleListService.getAllSampleListsInStudy(STUDY_ID, PROJECTION,
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedSampleLists, result);
    }

    @Test
    public void getMetaSampleListsInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(sampleListRepository.getMetaSampleListsInStudy(STUDY_ID)).thenReturn(expectedBaseMeta);

        BaseMeta result = sampleListService.getMetaSampleListsInStudy(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void getAllSampleIdsInSampleList() throws Exception {

        List<String> expectedSampleIds = new ArrayList<>();
        expectedSampleIds.add(SAMPLE_ID);
        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID)).thenReturn(expectedSampleIds);
        List<String> result = sampleListService.getAllSampleIdsInSampleList(SAMPLE_LIST_ID);

        Assert.assertEquals(expectedSampleIds, result);
    }
}