package org.cbioportal.service.impl;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
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
public class MolecularProfileServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private MolecularProfileServiceImpl molecularProfileService;

    @Mock
    private MolecularProfileRepository molecularProfileRepository;
    @Mock
    private StudyService studyService;

    @Test
    public void getAllMolecularProfiles() throws Exception {

        List<MolecularProfile> expectedMolecularProfileList = new ArrayList<>();
        MolecularProfile molecularProfile = new MolecularProfile();
        expectedMolecularProfileList.add(molecularProfile);

        Mockito.when(molecularProfileRepository.getAllMolecularProfiles(PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
                DIRECTION)).thenReturn(expectedMolecularProfileList);

        List<MolecularProfile> result = molecularProfileService.getAllMolecularProfiles(PROJECTION, PAGE_SIZE, 
            PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMolecularProfileList, result);
    }

    @Test
    public void getMetaMolecularProfiles() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(molecularProfileRepository.getMetaMolecularProfiles()).thenReturn(expectedBaseMeta);

        BaseMeta result = molecularProfileService.getMetaMolecularProfiles();

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = MolecularProfileNotFoundException.class)
    public void getMolecularProfileNotFound() throws Exception {

        Mockito.when(molecularProfileRepository.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(null);

        molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID);
    }

    @Test
    public void getMolecularProfile() throws Exception {

        MolecularProfile expectedMolecularProfile = new MolecularProfile();

        Mockito.when(molecularProfileRepository.getMolecularProfile(MOLECULAR_PROFILE_ID))
            .thenReturn(expectedMolecularProfile);

        MolecularProfile result = molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID);

        Assert.assertEquals(expectedMolecularProfile, result);
    }

    @Test
    public void getAllMolecularProfilesInStudy() throws Exception {

        List<MolecularProfile> expectedMolecularProfileList = new ArrayList<>();
        MolecularProfile molecularProfile = new MolecularProfile();
        expectedMolecularProfileList.add(molecularProfile);

        Mockito.when(molecularProfileRepository.getAllMolecularProfilesInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, 
            PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedMolecularProfileList);

        List<MolecularProfile> result = molecularProfileService.getAllMolecularProfilesInStudy(STUDY_ID, PROJECTION,
                PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMolecularProfileList, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getAllMolecularProfilesInStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        molecularProfileService.getAllMolecularProfilesInStudy(STUDY_ID, PROJECTION,
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaMolecularProfilesInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(molecularProfileRepository.getMetaMolecularProfilesInStudy(STUDY_ID)).thenReturn(expectedBaseMeta);

        BaseMeta result = molecularProfileService.getMetaMolecularProfilesInStudy(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getMetaMolecularProfilesInStudyNotFound() throws Exception {

        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        molecularProfileService.getMetaMolecularProfilesInStudy(STUDY_ID);
    }
}