package org.cbioportal.service.impl;

import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GeneticProfileRepository;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
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

@RunWith(MockitoJUnitRunner.class)
public class GeneticProfileServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private GeneticProfileServiceImpl geneticProfileService;

    @Mock
    private GeneticProfileRepository geneticProfileRepository;
    @Mock
    private StudyService studyService;

    @Test
    public void getAllGeneticProfiles() throws Exception {

        List<GeneticProfile> expectedGeneticProfileList = new ArrayList<>();
        GeneticProfile geneticProfile = new GeneticProfile();
        expectedGeneticProfileList.add(geneticProfile);

        Mockito.when(geneticProfileRepository.getAllGeneticProfiles(PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
                DIRECTION)).thenReturn(expectedGeneticProfileList);

        List<GeneticProfile> result = geneticProfileService.getAllGeneticProfiles(PROJECTION, PAGE_SIZE, PAGE_NUMBER,
                SORT, DIRECTION);

        Assert.assertEquals(expectedGeneticProfileList, result);
    }

    @Test
    public void getMetaGeneticProfiles() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(geneticProfileRepository.getMetaGeneticProfiles()).thenReturn(expectedBaseMeta);

        BaseMeta result = geneticProfileService.getMetaGeneticProfiles();

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = GeneticProfileNotFoundException.class)
    public void getGeneticProfileNotFound() throws Exception {

        Mockito.when(geneticProfileRepository.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(null);

        geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID);
    }

    @Test
    public void getGeneticProfile() throws Exception {

        GeneticProfile expectedGeneticProfile = new GeneticProfile();

        Mockito.when(geneticProfileRepository.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(expectedGeneticProfile);

        GeneticProfile result = geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID);

        Assert.assertEquals(expectedGeneticProfile, result);
    }

    @Test
    public void getAllGeneticProfilesInStudy() throws Exception {

        List<GeneticProfile> expectedGeneticProfileList = new ArrayList<>();
        GeneticProfile geneticProfile = new GeneticProfile();
        expectedGeneticProfileList.add(geneticProfile);

        Mockito.when(geneticProfileRepository.getAllGeneticProfilesInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER,
                SORT, DIRECTION)).thenReturn(expectedGeneticProfileList);

        List<GeneticProfile> result = geneticProfileService.getAllGeneticProfilesInStudy(STUDY_ID, PROJECTION,
                PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedGeneticProfileList, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getAllGeneticProfilesInStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        geneticProfileService.getAllGeneticProfilesInStudy(STUDY_ID, PROJECTION,
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaGeneticProfilesInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(geneticProfileRepository.getMetaGeneticProfilesInStudy(STUDY_ID)).thenReturn(expectedBaseMeta);

        BaseMeta result = geneticProfileService.getMetaGeneticProfilesInStudy(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getMetaGeneticProfilesInStudyNotFound() throws Exception {

        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        geneticProfileService.getMetaGeneticProfilesInStudy(STUDY_ID);
    }
}