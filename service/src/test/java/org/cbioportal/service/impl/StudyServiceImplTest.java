package org.cbioportal.service.impl;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MultiStudySample;
import org.cbioportal.model.StudyOverlap;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.service.CancerTypeService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class StudyServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private StudyServiceImpl studyService;

    @Mock
    private StudyRepository studyRepository;
    @Mock
    private CancerTypeService cancerTypeService;

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
        Mockito.when(cancerTypeService.getPrimarySiteMap()).thenReturn(new HashMap<>());

        List<CancerStudy> result = studyService.getAllStudies(KEYWORD, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedCancerStudyList, result);
    }

    @Test
    public void getMetaStudies() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(studyRepository.getMetaStudies(null)).thenReturn(expectedBaseMeta);

        BaseMeta result = studyService.getMetaStudies(null);

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

    @Test
    public void getStudiesWithOverlappingSamplesWhenNoneExist() {
        List<Integer> permittedStudyIds = Arrays.asList(1, 2);
        CancerStudy s1 = createStudy(1, "S1");
        CancerStudy s2 = createStudy(2, "S2");
        List<CancerStudy> permittedStudies = Arrays.asList(s1, s2);

        Mockito
            .when(studyRepository.getSamplesBelongingToMultipleStudies(permittedStudyIds))
            .thenReturn(new ArrayList<>());

        List<StudyOverlap> actual = studyService.getStudiesWithOverlappingSamples(permittedStudies);
        List<StudyOverlap> expected = new ArrayList<>();
        
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getStudiesWithOverlappingSamplesWhenSimpleOverlap() {
        /*
         * Studies S1 and S2 both share sample P1, so they overlap with each other
         */
        List<Integer> permittedStudyIds = Arrays.asList(1, 2);
        List<CancerStudy> permittedStudies = Arrays.asList(
            createStudy(1, "S1"),
            createStudy(2, "S2")
        );
        List<MultiStudySample> returnedSamples = Collections.singletonList(createSample("P1", 1, 2));


        Mockito
            .when(studyRepository.getSamplesBelongingToMultipleStudies(permittedStudyIds))
            .thenReturn(returnedSamples);

        List<StudyOverlap> actual = studyService.getStudiesWithOverlappingSamples(permittedStudies);
        List<StudyOverlap> expected = Arrays.asList(
            createOverlap("S1", "S2"),
            createOverlap("S2", "S1")
        );

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void getStudiesWithOverlappingSamplesWhenComplexOverlap() {
        /*
         * Studies S1 and S2 both share sample P12, so they overlap with each other.
         * Studies S1 and S3 both share sample P13, so they overlap with each other.
         * So:
         *  S1 overlaps with S2, S3
         *  S2 overlaps with S1
         *  S3 overlaps with S1
         */
        List<Integer> permittedStudyIds = Arrays.asList(1, 2, 3);
        List<CancerStudy> permittedStudies = Arrays.asList(
            createStudy(1, "S1"),
            createStudy(2, "S2"),
            createStudy(3, "S3")
        );
        List<MultiStudySample> returnedSamples = Arrays.asList(
            createSample("P12", 1, 2),
            createSample("P13", 1, 3)
        );

        Mockito
            .when(studyRepository.getSamplesBelongingToMultipleStudies(permittedStudyIds))
            .thenReturn(returnedSamples);

        List<StudyOverlap> actual = studyService.getStudiesWithOverlappingSamples(permittedStudies);
        List<StudyOverlap> expected = Arrays.asList(
            createOverlap("S1", "S2", "S3"),
            createOverlap("S2", "S1"),
            createOverlap("S3", "S1")
        );

        Assert.assertEquals(actual, expected);
    }
    
    private CancerStudy createStudy(Integer id, String identifier) {
        CancerStudy cancerStudy = new CancerStudy();
        cancerStudy.setCancerStudyId(id);
        cancerStudy.setCancerStudyIdentifier(identifier);
        return cancerStudy;
    }
    
    private MultiStudySample createSample(String id, Integer... sampleIds) {
        MultiStudySample multiStudySample = new MultiStudySample();
        multiStudySample.setSampleId(id);
        multiStudySample.setStudyIdentifiers(new HashSet<>(Arrays.asList(sampleIds)));
        return multiStudySample;
    }
    
    private StudyOverlap createOverlap(String id, String... overlappingStudyIds) {
        StudyOverlap overlap = new StudyOverlap(id);
        overlap.setOverlappingStudyIds(new HashSet<>(Arrays.asList(overlappingStudyIds)));
        return overlap;
    }
}
