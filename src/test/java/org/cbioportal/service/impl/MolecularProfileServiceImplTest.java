package org.cbioportal.service.impl;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MolecularProfileServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private MolecularProfileServiceImpl molecularProfileService;

    @Mock
    private MolecularProfileRepository molecularProfileRepository;
    @Mock
    private StudyService studyService;
    @Mock
    private MolecularProfileUtil molecularProfileUtil;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(molecularProfileService, "AUTHENTICATE", "false");
    }

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
    public void getMolecularProfiles() throws Exception {

        List<MolecularProfile> expectedMolecularProfiles = new ArrayList<>();

        Mockito.when(molecularProfileRepository.getMolecularProfiles(Collections.singleton(MOLECULAR_PROFILE_ID), PROJECTION))
            .thenReturn(expectedMolecularProfiles);

        List<MolecularProfile> result = molecularProfileService.getMolecularProfiles(Collections.singleton(MOLECULAR_PROFILE_ID), PROJECTION);

        Assert.assertEquals(expectedMolecularProfiles, result);
    }

    @Test
    public void getMetaMolecularProfilesById() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(molecularProfileRepository.getMetaMolecularProfiles(Collections.singleton(MOLECULAR_PROFILE_ID)))
            .thenReturn(expectedBaseMeta);

        BaseMeta result = molecularProfileService.getMetaMolecularProfiles(Collections.singleton(MOLECULAR_PROFILE_ID));

        Assert.assertEquals(expectedBaseMeta, result);
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

    @Test
    public void getMolecularProfilesInStudies() throws Exception {

        List<MolecularProfile> expectedMolecularProfileList = new ArrayList<>();
        MolecularProfile molecularProfile = new MolecularProfile();
        expectedMolecularProfileList.add(molecularProfile);

        Mockito.when(molecularProfileRepository.getMolecularProfilesInStudies(Arrays.asList(STUDY_ID), PROJECTION))
            .thenReturn(expectedMolecularProfileList);

        List<MolecularProfile> result = molecularProfileService.getMolecularProfilesInStudies(
            Arrays.asList(STUDY_ID), PROJECTION);

        Assert.assertEquals(expectedMolecularProfileList, result);
    }

    @Test
    public void getMetaMolecularProfilesInStudies() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(molecularProfileRepository.getMetaMolecularProfilesInStudies(Arrays.asList(STUDY_ID)))
            .thenReturn(expectedBaseMeta);

        BaseMeta result = molecularProfileService.getMetaMolecularProfilesInStudies(Arrays.asList(STUDY_ID));

        Assert.assertEquals(expectedBaseMeta, result);
    }
    
    @Test
    public void getMolecularProfilesReferredBy() throws Exception {
        List<MolecularProfile> expectedMolecularProfileList = new ArrayList<>();
        MolecularProfile molecularProfile = new MolecularProfile();
        expectedMolecularProfileList.add(molecularProfile);

        Mockito.when(molecularProfileRepository.getMolecularProfile(MOLECULAR_PROFILE_ID))
                .thenReturn(molecularProfile);
        Mockito.when(molecularProfileRepository.getMolecularProfilesReferredBy(MOLECULAR_PROFILE_ID))
            .thenReturn(expectedMolecularProfileList);

        List<MolecularProfile> result = molecularProfileService.getMolecularProfilesReferredBy(
            MOLECULAR_PROFILE_ID);

        Assert.assertEquals(expectedMolecularProfileList, result);
    }

    @Test
    public void getMolecularProfilesReferringTo() throws Exception {
        List<MolecularProfile> expectedMolecularProfileList = new ArrayList<>();
        MolecularProfile molecularProfile = new MolecularProfile();
        expectedMolecularProfileList.add(molecularProfile);

        Mockito.when(molecularProfileRepository.getMolecularProfile(MOLECULAR_PROFILE_ID))
            .thenReturn(molecularProfile);
        Mockito.when(molecularProfileRepository.getMolecularProfilesReferringTo(MOLECULAR_PROFILE_ID))
            .thenReturn(expectedMolecularProfileList);

        List<MolecularProfile> result = molecularProfileService.getMolecularProfilesReferringTo(
            MOLECULAR_PROFILE_ID);

        Assert.assertEquals(expectedMolecularProfileList, result);
    }
}
