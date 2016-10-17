package org.cbioportal.service.impl;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class StudyServiceImplTest {

    public static final String STUDY_ID = "study_id";
    public static final String PROJECTION = "projection";
    public static final Integer PAGE_SIZE = 1000;
    public static final Integer PAGE_NUMBER = 0;
    public static final String SORT = "sort";
    public static final String DIRECTION = "direction";

    @InjectMocks
    private StudyServiceImpl studyService;

    @Mock
    private StudyRepository studyRepository;

    @Test
    public void getAllStudies() throws Exception {

        List<CancerStudy> expectedCancerStudyList = new ArrayList<>();
        CancerStudy cancerStudy = new CancerStudy();
        expectedCancerStudyList.add(cancerStudy);

        Mockito.when(studyRepository.getAllStudies(PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
                .thenReturn(expectedCancerStudyList);

        List<CancerStudy> result = studyService.getAllStudies(PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedCancerStudyList, result);
    }

    @Test
    public void getMetaStudies() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(studyRepository.getMetaStudies()).thenReturn(expectedBaseMeta);

        BaseMeta result = studyService.getMetaStudies();

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getStudyNotFound() throws Exception {

        Mockito.when(studyRepository.getStudy(STUDY_ID)).thenReturn(null);

        studyService.getStudy(STUDY_ID);
    }

    @Test
    public void getStudy() throws Exception {

        CancerStudy expectedCancerStudy = new CancerStudy();

        Mockito.when(studyRepository.getStudy(STUDY_ID)).thenReturn(expectedCancerStudy);

        CancerStudy result = studyService.getStudy(STUDY_ID);

        Assert.assertEquals(expectedCancerStudy, result);
    }
}