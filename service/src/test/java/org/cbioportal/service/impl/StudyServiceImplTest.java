package org.cbioportal.service.impl;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class StudyServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private StudyServiceImpl studyService;

    @Mock
    private StudyRepository studyRepository;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(studyService, "AUTHENTICATE", "false");
    }

    @Test
    public void getAllStudies() throws Exception {

        List<CancerStudy> expectedCancerStudyList = new ArrayList<>();
        CancerStudy cancerStudy = new CancerStudy();
        expectedCancerStudyList.add(cancerStudy);

        Mockito.when(studyRepository.getAllStudies(KEYWORD, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
                .thenReturn(expectedCancerStudyList);

        List<CancerStudy> result = studyService.getAllStudies(KEYWORD, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedCancerStudyList, result);
    }

    @Test
    public void getMetaStudies() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(studyRepository.getMetaStudies(KEYWORD)).thenReturn(expectedBaseMeta);

        BaseMeta result = studyService.getMetaStudies(KEYWORD);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getStudyNotFound() throws Exception {

        Mockito.when(studyRepository.getStudy(STUDY_ID, "DETAILED")).thenReturn(null);

        studyService.getStudy(STUDY_ID);
    }

    @Test
    public void getStudy() throws Exception {

        CancerStudy expectedCancerStudy = new CancerStudy();

        Mockito.when(studyRepository.getStudy(STUDY_ID, "DETAILED")).thenReturn(expectedCancerStudy);

        CancerStudy result = studyService.getStudy(STUDY_ID);

        Assert.assertEquals(expectedCancerStudy, result);
    }

    @Test
    public void fetchStudies() throws Exception {

        List<CancerStudy> expectedCancerStudyList = new ArrayList<>();
        CancerStudy cancerStudy = new CancerStudy();
        expectedCancerStudyList.add(cancerStudy);

        Mockito.when(studyRepository.fetchStudies(Arrays.asList(STUDY_ID), PROJECTION))
                .thenReturn(expectedCancerStudyList);

        List<CancerStudy> result = studyService.fetchStudies(Arrays.asList(STUDY_ID), PROJECTION);

        Assert.assertEquals(expectedCancerStudyList, result);
    }

    @Test
    public void fetchMetaStudies() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(studyRepository.fetchMetaStudies(Arrays.asList(STUDY_ID))).thenReturn(expectedBaseMeta);

        BaseMeta result = studyService.fetchMetaStudies(Arrays.asList(STUDY_ID));

        Assert.assertEquals(expectedBaseMeta, result);
    }
}
