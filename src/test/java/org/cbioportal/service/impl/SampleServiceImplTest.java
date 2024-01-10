package org.cbioportal.service.impl;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CopyNumberSegmentRepository;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SampleServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private SampleServiceImpl sampleService;

    @Mock
    private SampleRepository sampleRepository;
    @Mock
    private StudyService studyService;
    @Mock
    private PatientService patientService;
    @Mock
    private SampleListRepository sampleListRepository;
    @Mock
    private CopyNumberSegmentRepository copyNumberSegmentRepository;
    @Mock
    private MolecularProfileRepository molecularProfileRepository;
    
    private Sample createSample(String id) {
        Sample sample = new Sample();
        sample.setStableId(id);
        return sample;
    }
    
    @Test
    public void getAllSamples() {
        List<Sample> samples = Arrays.asList(
            createSample(SAMPLE_ID1), createSample(SAMPLE_ID2),
            createSample(SAMPLE_ID3), createSample(SAMPLE_ID4)
        );
        Mockito
            .when(sampleRepository.getAllSamples("sample_id", null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
            .thenReturn(samples);

        List<Sample> result = sampleService.getAllSamples("sample_id", null, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
        List<String> actual = result.stream().map(Sample::getStableId).collect(Collectors.toList());
        List<String> expected = Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3, SAMPLE_ID4);
        
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void getAllMetaSamples() {
        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(4);
        Mockito.when(sampleRepository.getMetaSamples("sample_id", null)).thenReturn(baseMeta);

        BaseMeta result = sampleService.getMetaSamples("sample_id", null);
        Integer actual = result.getTotalCount();
        Integer expected = 4;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getAllSamplesInStudy() throws Exception {

        List<Sample> expectedSampleList = new ArrayList<>();
        Sample sample = new Sample();
        expectedSampleList.add(sample);

        Mockito.when(sampleRepository.getAllSamplesInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
                DIRECTION)).thenReturn(expectedSampleList);
        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(Mockito.anyString()))
            .thenReturn(new ArrayList<>());
        Mockito.when(copyNumberSegmentRepository.fetchCopyNumberSegments(Mockito.anyList(), 
            Mockito.anyList(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList<>());

        List<Sample> result = sampleService.getAllSamplesInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
                DIRECTION);

        Assert.assertEquals(expectedSampleList, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getAllSamplesInStudyNotFound() throws Exception {

        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        sampleService.getAllSamplesInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaSamplesInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(sampleRepository.getMetaSamplesInStudy(STUDY_ID)).thenReturn(expectedBaseMeta);
        BaseMeta result = sampleService.getMetaSamplesInStudy(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getMetaSamplesInStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        sampleService.getMetaSamplesInStudy(STUDY_ID);
    }

    @Test(expected = SampleNotFoundException.class)
    public void getSampleInStudySampleNotFound() throws Exception {

        Mockito.when(sampleRepository.getSampleInStudy(STUDY_ID, SAMPLE_ID1)).thenReturn(null);
        sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getSampleInStudyNotFound() throws Exception {

        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1);
    }

    @Test
    public void getSampleInStudy() throws Exception {

        Sample expectedSample = new Sample();
        Mockito.when(sampleRepository.getSampleInStudy(STUDY_ID, SAMPLE_ID1)).thenReturn(expectedSample);
        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(Mockito.anyString()))
            .thenReturn(new ArrayList<>());
        Mockito.when(copyNumberSegmentRepository.fetchCopyNumberSegments(Mockito.anyList(),
            Mockito.anyList(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList<>());
        
        Sample result = sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1);

        Assert.assertEquals(expectedSample, result);
    }

    @Test
    public void getAllSamplesOfPatientInStudy() throws Exception {

        List<Sample> expectedSampleList = new ArrayList<>();
        Sample sample = new Sample();
        expectedSampleList.add(sample);

        Mockito.when(sampleRepository.getAllSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID_1, PROJECTION, PAGE_SIZE,
                PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedSampleList);
        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(Mockito.anyString()))
            .thenReturn(new ArrayList<>());
        Mockito.when(copyNumberSegmentRepository.fetchCopyNumberSegments(Mockito.anyList(),
            Mockito.anyList(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList<>());

        List<Sample> result = sampleService.getAllSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID_1, PROJECTION, PAGE_SIZE,
                PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedSampleList, result);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getAllSamplesOfPatientInStudyPatientNotFound() throws Exception {

        Mockito.when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenThrow(new PatientNotFoundException(
            STUDY_ID, PATIENT_ID_1));
        sampleService.getAllSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID_1, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, 
            DIRECTION);
    }

    @Test
    public void getMetaSamplesOfPatientInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(sampleRepository.getMetaSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenReturn(expectedBaseMeta);
        BaseMeta result = sampleService.getMetaSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID_1);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getMetaSamplesOfPatientInStudyPatientNotFound() throws Exception {
        
        Mockito.when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenThrow(new PatientNotFoundException(
            STUDY_ID, PATIENT_ID_1));
        sampleService.getMetaSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID_1);
    }

    @Test
    public void getAllSamplesOfPatientsInStudy() throws Exception {

        List<Sample> expectedSampleList = new ArrayList<>();
        Sample sample = new Sample();
        expectedSampleList.add(sample);

        Mockito.when(sampleRepository.getAllSamplesOfPatientsInStudy(STUDY_ID, Arrays.asList(PATIENT_ID_1), PROJECTION))
            .thenReturn(expectedSampleList);
        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(Mockito.anyString()))
            .thenReturn(new ArrayList<>());
        Mockito.when(copyNumberSegmentRepository.fetchCopyNumberSegments(Mockito.anyList(),
            Mockito.anyList(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList<>());

        List<Sample> result = sampleService.getAllSamplesOfPatientsInStudy(STUDY_ID, Arrays.asList(PATIENT_ID_1), PROJECTION);

        Assert.assertEquals(expectedSampleList, result);
    }

    @Test
    public void fetchSamples() throws Exception {

        List<Sample> expectedSampleList = new ArrayList<>();
        Sample sample = new Sample();
        expectedSampleList.add(sample);

        Mockito.when(sampleRepository.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), PROJECTION))
                .thenReturn(expectedSampleList);
        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(Mockito.anyString()))
            .thenReturn(new ArrayList<>());
        Mockito.when(copyNumberSegmentRepository.fetchCopyNumberSegments(Mockito.anyList(),
            Mockito.anyList(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ArrayList<>());

        List<Sample> result = sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), PROJECTION);

        Assert.assertEquals(expectedSampleList, result);
    }
    
    @Test
    public void fetchSamplesDetailed() throws Exception {
        List<Sample> expectedSampleList = new ArrayList<>();
        Sample sample1 = new Sample();
        Sample sample2 = new Sample();
        expectedSampleList.add(sample1);
        expectedSampleList.add(sample2);
        
        sample1.setCancerStudyIdentifier(STUDY_ID);
        sample1.setStableId(SAMPLE_ID1);
        sample1.setInternalId(SAMPLE_INTERNAL_ID);
        sample2.setCancerStudyIdentifier(STUDY_ID);
        sample2.setStableId(SAMPLE_ID2);
        sample2.setInternalId(SAMPLE_INTERNAL_ID2);
        
        List<Integer> expectedInternalIdList = new ArrayList<>();
        expectedInternalIdList.add(SAMPLE_INTERNAL_ID);
       
        
        Mockito.when(sampleRepository.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), "DETAILED"))
                .thenReturn(expectedSampleList);
        Mockito.when(sampleListRepository.getAllSampleIdsInSampleList(Mockito.anyString()))
            .thenReturn(new ArrayList<>());
        Mockito.when(copyNumberSegmentRepository.fetchSamplesWithCopyNumberSegments(Mockito.anyList(),
            Mockito.anyList(), Mockito.any())).thenReturn(expectedInternalIdList);

        List<MolecularProfile> expectedMolecularProfileList = new ArrayList<>();
        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.STRUCTURAL_VARIANT);
        expectedMolecularProfileList.add(molecularProfile);

        Mockito.when(molecularProfileRepository.getMolecularProfilesInStudies(Arrays.asList(STUDY_ID), "DETAILED"))
            .thenReturn(expectedMolecularProfileList);
        
        List<Sample> result = sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), "DETAILED");
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.get(0).getCopyNumberSegmentPresent());
        Assert.assertFalse(result.get(1).getCopyNumberSegmentPresent());
    }

    @Test
    public void fetchSamplesBySampleListIds() throws Exception {

        List<Sample> expectedSampleList = new ArrayList<>();
        Sample sample = new Sample();
        expectedSampleList.add(sample);

        Mockito.when(sampleRepository.fetchSamplesBySampleListIds(Arrays.asList(SAMPLE_LIST_ID), PROJECTION))
                .thenReturn(expectedSampleList);

        List<Sample> result = sampleService.fetchSamples(Arrays.asList(SAMPLE_LIST_ID), PROJECTION);

        Assert.assertEquals(expectedSampleList, result);
    }

    @Test
    public void fetchMetaSamples() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(sampleRepository.fetchMetaSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1)))
                .thenReturn(expectedBaseMeta);
        BaseMeta result = sampleService.fetchMetaSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1));

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void fetchMetaSamplesBySampleListIds() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(sampleRepository.fetchMetaSamples(Arrays.asList(SAMPLE_LIST_ID))).thenReturn(expectedBaseMeta);
        BaseMeta result = sampleService.fetchMetaSamples(Arrays.asList(SAMPLE_LIST_ID));

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void getSamplesByInternalIds() throws Exception {

        List<Sample> expectedSampleList = new ArrayList<>();
        Sample sample = new Sample();
        expectedSampleList.add(sample);

        Mockito.when(sampleRepository.getSamplesByInternalIds(Arrays.asList(SAMPLE_INTERNAL_ID)))
            .thenReturn(expectedSampleList);

        List<Sample> result = sampleService.getSamplesByInternalIds(Arrays.asList(SAMPLE_INTERNAL_ID));

        Assert.assertEquals(expectedSampleList, result);
    }
}
