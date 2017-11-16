package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.ClinicalAttributeNotFoundException;
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
public class ClinicalAttributeServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private ClinicalAttributeServiceImpl clinicalAttributeService;
    
    @Mock
    private ClinicalAttributeRepository clinicalAttributeRepository;
    @Mock
    private StudyService studyService;
    
    @Test
    public void getAllClinicalAttributes() throws Exception {

        List<ClinicalAttribute> expectedClinicalAttributeList = new ArrayList<>();
        ClinicalAttribute clinicalAttribute = new ClinicalAttribute();
        expectedClinicalAttributeList.add(clinicalAttribute);

        Mockito.when(clinicalAttributeRepository.getAllClinicalAttributes(PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, 
            DIRECTION)).thenReturn(expectedClinicalAttributeList);
        
        List<ClinicalAttribute> result = clinicalAttributeService.getAllClinicalAttributes(PROJECTION, PAGE_SIZE, 
            PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedClinicalAttributeList, result);
    }

    @Test
    public void getMetaClinicalAttributes() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(clinicalAttributeRepository.getMetaClinicalAttributes()).thenReturn(expectedBaseMeta);
        BaseMeta result = clinicalAttributeService.getMetaClinicalAttributes();

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = ClinicalAttributeNotFoundException.class)
    public void getClinicalAttributeNotFound() throws Exception {
        
        Mockito.when(clinicalAttributeRepository.getClinicalAttribute(STUDY_ID, CLINICAL_ATTRIBUTE_ID))
            .thenReturn(null);
        clinicalAttributeService.getClinicalAttribute(STUDY_ID, CLINICAL_ATTRIBUTE_ID);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getClinicalAttributeStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        clinicalAttributeService.getClinicalAttribute(STUDY_ID, CLINICAL_ATTRIBUTE_ID);
    }

    @Test
    public void getClinicalAttribute() throws Exception {
        
        ClinicalAttribute expectedClinicalAttribute = new ClinicalAttribute();
        
        Mockito.when(clinicalAttributeRepository.getClinicalAttribute(STUDY_ID, CLINICAL_ATTRIBUTE_ID))
            .thenReturn(expectedClinicalAttribute);
        
        ClinicalAttribute result = clinicalAttributeService.getClinicalAttribute(STUDY_ID, CLINICAL_ATTRIBUTE_ID);
        
        Assert.assertEquals(expectedClinicalAttribute, result);
    }

    @Test
    public void getAllClinicalAttributesInStudy() throws Exception {

        List<ClinicalAttribute> expectedClinicalAttributeList = new ArrayList<>();
        ClinicalAttribute clinicalAttribute = new ClinicalAttribute();
        expectedClinicalAttributeList.add(clinicalAttribute);

        Mockito.when(clinicalAttributeRepository.getAllClinicalAttributesInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, 
            PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedClinicalAttributeList);

        List<ClinicalAttribute> result = clinicalAttributeService.getAllClinicalAttributesInStudy(STUDY_ID, PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedClinicalAttributeList, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getAllClinicalAttributesInStudyNotFound() throws Exception {

        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        clinicalAttributeService.getAllClinicalAttributesInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, 
            DIRECTION);
    }

    @Test
    public void getMetaClinicalAttributesInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(clinicalAttributeRepository.getMetaClinicalAttributesInStudy(STUDY_ID)).thenReturn(expectedBaseMeta);
        BaseMeta result = clinicalAttributeService.getMetaClinicalAttributesInStudy(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getMetaClinicalAttributesInStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        clinicalAttributeService.getMetaClinicalAttributesInStudy(STUDY_ID);
    }
}