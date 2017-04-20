package org.cbioportal.service.impl;

import org.cbioportal.model.SampleList;
import org.cbioportal.model.SampleListSampleCount;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SampleListServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private SampleListServiceImpl sampleListService;

    @Mock
    private SampleListRepository sampleListRepository;
    @Mock
    private StudyService studyService;

    @Test
    public void getAllSampleLists() throws Exception {

        List<SampleList> expectedSampleLists = new ArrayList<>();
        SampleList sampleList = new SampleList();
        sampleList.setListId(1);
        expectedSampleLists.add(sampleList);
        
        List<SampleListSampleCount> expectedSampleListSampleCounts = new ArrayList<>();
        SampleListSampleCount sampleListSampleCount = new SampleListSampleCount();
        sampleListSampleCount.setSampleListId(1);
        sampleListSampleCount.setSampleCount(10);
        expectedSampleListSampleCounts.add(sampleListSampleCount);
        
        Mockito.when(sampleListRepository.getSampleCounts(Arrays.asList(1))).thenReturn(expectedSampleListSampleCounts);

        Mockito.when(sampleListRepository.getAllSampleLists("DETAILED", PAGE_SIZE, PAGE_NUMBER, SORT,
            DIRECTION)).thenReturn(expectedSampleLists);

        List<SampleList> result = sampleListService.getAllSampleLists("DETAILED", PAGE_SIZE, PAGE_NUMBER,
            SORT, DIRECTION);

        Assert.assertEquals(expectedSampleLists, result);
        Assert.assertEquals((Integer) 10, expectedSampleLists.get(0).getSampleCount());
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
        expectedSampleList.setListId(1);

        Mockito.when(sampleListRepository.getSampleList(SAMPLE_LIST_ID)).thenReturn(expectedSampleList);

        List<SampleListSampleCount> expectedSampleListSampleCounts = new ArrayList<>();
        SampleListSampleCount sampleListSampleCount = new SampleListSampleCount();
        sampleListSampleCount.setSampleCount(10);
        expectedSampleListSampleCounts.add(sampleListSampleCount);

        Mockito.when(sampleListRepository.getSampleCounts(Arrays.asList(1))).thenReturn(expectedSampleListSampleCounts);
        
        SampleList result = sampleListService.getSampleList(SAMPLE_LIST_ID);

        Assert.assertEquals(expectedSampleList, result);
        Assert.assertEquals((Integer) 10, result.getSampleCount());
    }

    @Test
    public void getAllSampleListsInStudy() throws Exception {

        List<SampleList> expectedSampleLists = new ArrayList<>();
        SampleList sampleList = new SampleList();
        sampleList.setListId(1);
        expectedSampleLists.add(sampleList);

        List<SampleListSampleCount> expectedSampleListSampleCounts = new ArrayList<>();
        SampleListSampleCount sampleListSampleCount = new SampleListSampleCount();
        sampleListSampleCount.setSampleListId(1);
        sampleListSampleCount.setSampleCount(10);
        expectedSampleListSampleCounts.add(sampleListSampleCount);

        Mockito.when(sampleListRepository.getSampleCounts(Arrays.asList(1))).thenReturn(expectedSampleListSampleCounts);

        Mockito.when(sampleListRepository.getAllSampleListsInStudy(STUDY_ID, "DETAILED", PAGE_SIZE, PAGE_NUMBER,
            SORT, DIRECTION)).thenReturn(expectedSampleLists);

        List<SampleList> result = sampleListService.getAllSampleListsInStudy(STUDY_ID, "DETAILED", PAGE_SIZE, 
            PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedSampleLists, result);
        Assert.assertEquals((Integer) 10, expectedSampleLists.get(0).getSampleCount());
    }

    @Test(expected = StudyNotFoundException.class)
    public void getAllSampleListsInStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        sampleListService.getAllSampleListsInStudy(STUDY_ID, "DETAILED", PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaSampleListsInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(sampleListRepository.getMetaSampleListsInStudy(STUDY_ID)).thenReturn(expectedBaseMeta);

        BaseMeta result = sampleListService.getMetaSampleListsInStudy(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getMetaSampleListsInStudyNotFound() throws Exception {

        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        sampleListService.getMetaSampleListsInStudy(STUDY_ID);
    }

    @Test
    public void getAllSampleIdsInSampleList() throws Exception {

        SampleList expectedSampleList = new SampleList();
        expectedSampleList.setListId(1);

        Mockito.when(sampleListRepository.getSampleList(SAMPLE_LIST_ID)).thenReturn(expectedSampleList);

        List<SampleListSampleCount> expectedSampleListSampleCounts = new ArrayList<>();
        SampleListSampleCount sampleListSampleCount = new SampleListSampleCount();
        sampleListSampleCount.setSampleCount(10);
        expectedSampleListSampleCounts.add(sampleListSampleCount);

        Mockito.when(sampleListRepository.getSampleCounts(Arrays.asList(1))).thenReturn(expectedSampleListSampleCounts);

        List<String> expectedSampleIds = new ArrayList<>();
        expectedSampleIds.add(SAMPLE_ID);
        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID)).thenReturn(expectedSampleIds);
        List<String> result = sampleListService.getAllSampleIdsInSampleList(SAMPLE_LIST_ID);

        Assert.assertEquals(expectedSampleIds, result);
    }

    @Test(expected = SampleListNotFoundException.class)
    public void getAllSampleIdsInSampleListNotFound() throws Exception {

        Mockito.when(sampleListRepository.getSampleList(SAMPLE_LIST_ID)).thenReturn(null);
        sampleListService.getAllSampleIdsInSampleList(SAMPLE_LIST_ID);
    }
}