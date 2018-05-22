package org.cbioportal.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.Sample;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleService;
import org.cbioportal.web.parameter.ClinicalDataEqualityFilter;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.CopyNumberGeneFilter;
import org.cbioportal.web.parameter.CopyNumberGeneFilterElement;
import org.cbioportal.web.parameter.MutationGeneFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StudyViewFilterApplierTest {

    public static final String STUDY_ID = "study_id";
    public static final String SAMPLE_ID1 = "sample_id1";
    public static final String SAMPLE_ID2 = "sample_id2";
    public static final String SAMPLE_ID3 = "sample_id3";
    public static final String SAMPLE_ID4 = "sample_id4";
    public static final String SAMPLE_ID5 = "sample_id5";
    public static final String PATIENT_ID1 = "patient_id1";
    public static final String PATIENT_ID2 = "patient_id2";
    public static final String PATIENT_ID3 = "patient_id3";
    public static final String PATIENT_ID4 = "patient_id4";
    public static final String CLINICAL_ATTRIBUTE_ID_1 = "attribute_id1";
    public static final String CLINICAL_ATTRIBUTE_ID_2 = "attribute_id2";
    public static final Integer ENTREZ_GENE_ID_1 = 1;
    public static final Integer ENTREZ_GENE_ID_2 = 2;
    public static final String MOLECULAR_PROFILE_ID_1 = "molecular_profile_id1";
    public static final String MOLECULAR_PROFILE_ID_2 = "molecular_profile_id2";

    @InjectMocks
    private StudyViewFilterApplier studyViewFilterApplier;
    
    @Mock
    private SampleService sampleService;
    @Mock
    private PatientService patientService;
    @Mock
    private ClinicalDataService clinicalDataService;
    @Mock
    private MutationService mutationService;
    @Mock
    private DiscreteCopyNumberService discreteCopyNumberService;

    @Test
    public void apply() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add(STUDY_ID);
        studyIds.add(STUDY_ID);
        studyIds.add(STUDY_ID);
        studyIds.add(STUDY_ID);
        studyIds.add(STUDY_ID);
    
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(SAMPLE_ID1);
        sampleIds.add(SAMPLE_ID2);
        sampleIds.add(SAMPLE_ID3);
        sampleIds.add(SAMPLE_ID4);
        sampleIds.add(SAMPLE_ID5);
        studyViewFilter.setSampleIds(sampleIds);
        List<ClinicalDataEqualityFilter> clinicalDataEqualityFilters = new ArrayList<>();
        ClinicalDataEqualityFilter clinicalDataEqualityFilter1 = new ClinicalDataEqualityFilter();
        clinicalDataEqualityFilter1.setAttributeId(CLINICAL_ATTRIBUTE_ID_1);
        clinicalDataEqualityFilter1.setClinicalDataType(ClinicalDataType.SAMPLE);
        clinicalDataEqualityFilter1.setValues(Arrays.asList("value1"));
        clinicalDataEqualityFilters.add(clinicalDataEqualityFilter1);
        ClinicalDataEqualityFilter clinicalDataEqualityFilter2 = new ClinicalDataEqualityFilter();
        clinicalDataEqualityFilter2.setAttributeId(CLINICAL_ATTRIBUTE_ID_2);
        clinicalDataEqualityFilter2.setClinicalDataType(ClinicalDataType.PATIENT);
        clinicalDataEqualityFilter2.setValues(Arrays.asList("value2", "NA"));
        clinicalDataEqualityFilters.add(clinicalDataEqualityFilter2);
        studyViewFilter.setClinicalDataEqualityFilters(clinicalDataEqualityFilters);
        List<MutationGeneFilter> mutatedGenes = new ArrayList<>();
        MutationGeneFilter mutationGeneFilter = new MutationGeneFilter();
        mutationGeneFilter.setEntrezGeneIds(Arrays.asList(ENTREZ_GENE_ID_1));
        mutationGeneFilter.setMolecularProfileId(MOLECULAR_PROFILE_ID_1);
        mutatedGenes.add(mutationGeneFilter);
        studyViewFilter.setMutatedGenes(mutatedGenes);
        List<CopyNumberGeneFilter> cnaGenes = new ArrayList<>();
        CopyNumberGeneFilter copyNumberGeneFilter = new CopyNumberGeneFilter();
        List<CopyNumberGeneFilterElement> copyNumberGeneFilterElements = new ArrayList<>();
        CopyNumberGeneFilterElement copyNumberGeneFilterElement = new CopyNumberGeneFilterElement();
        copyNumberGeneFilterElement.setAlteration(-2);
        copyNumberGeneFilterElement.setEntrezGeneId(ENTREZ_GENE_ID_2);
        copyNumberGeneFilterElements.add(copyNumberGeneFilterElement);
        copyNumberGeneFilter.setAlterations(copyNumberGeneFilterElements);
        copyNumberGeneFilter.setMolecularProfileId(MOLECULAR_PROFILE_ID_2);
        cnaGenes.add(copyNumberGeneFilter);
        studyViewFilter.setCnaGenes(cnaGenes);

        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setStableId(SAMPLE_ID1);
        samples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setStableId(SAMPLE_ID2);
        samples.add(sample2);
        Sample sample3 = new Sample();
        sample3.setStableId(SAMPLE_ID3);
        samples.add(sample3);
        Sample sample4 = new Sample();
        sample4.setStableId(SAMPLE_ID4);
        samples.add(sample4);
        Sample sample5 = new Sample();
        sample5.setStableId(SAMPLE_ID5);
        samples.add(sample5);

        List<String> patientIds = new ArrayList<>();
        patientIds.add(PATIENT_ID1);
        patientIds.add(PATIENT_ID2);
        patientIds.add(PATIENT_ID3);
        patientIds.add(PATIENT_ID4);

        Mockito.when(sampleService.fetchSamples(studyIds, sampleIds, "ID")).thenReturn(samples);
        Mockito.when(patientService.getPatientIdsOfSamples(sampleIds)).thenReturn(patientIds);

        List<ClinicalData> sampleClinicalDataList = new ArrayList<>();
        ClinicalData sampleClinicalData1 = new ClinicalData();
        sampleClinicalData1.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalData1.setAttrValue("value1");
        sampleClinicalData1.setSampleId(SAMPLE_ID1);
        sampleClinicalDataList.add(sampleClinicalData1);
        ClinicalData sampleClinicalData2 = new ClinicalData();
        sampleClinicalData2.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalData2.setAttrValue("value1");
        sampleClinicalData2.setSampleId(SAMPLE_ID2);
        sampleClinicalDataList.add(sampleClinicalData2);
        ClinicalData sampleClinicalData3 = new ClinicalData();
        sampleClinicalData3.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalData3.setAttrValue("NAN");
        sampleClinicalData3.setSampleId(SAMPLE_ID3);
        sampleClinicalDataList.add(sampleClinicalData3);
        ClinicalData sampleClinicalData4 = new ClinicalData();
        sampleClinicalData4.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalData4.setAttrValue("value1");
        sampleClinicalData4.setSampleId(SAMPLE_ID4);
        sampleClinicalDataList.add(sampleClinicalData4);
        ClinicalData sampleClinicalData5 = new ClinicalData();
        sampleClinicalData5.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalData5.setAttrValue("value1");
        sampleClinicalData5.setSampleId(SAMPLE_ID5);
        sampleClinicalDataList.add(sampleClinicalData5);

        Mockito.when(clinicalDataService.fetchAllClinicalDataInStudy(STUDY_ID, sampleIds, 
            Arrays.asList(CLINICAL_ATTRIBUTE_ID_1), "SAMPLE", "SUMMARY")).thenReturn(sampleClinicalDataList);

        List<String> updatedSampleIds = new ArrayList<>();
        updatedSampleIds.add(SAMPLE_ID5);
        updatedSampleIds.add(SAMPLE_ID4);
        updatedSampleIds.add(SAMPLE_ID1);
        updatedSampleIds.add(SAMPLE_ID2);

        List<Sample> updatedSamples = new ArrayList<>();
        updatedSamples.add(sample1);
        updatedSamples.add(sample2);
        updatedSamples.add(sample4);
        updatedSamples.add(sample5);

        List<String> updatedPatientIds = new ArrayList<>();
        updatedPatientIds.add(PATIENT_ID1);
        updatedPatientIds.add(PATIENT_ID2);
        updatedPatientIds.add(PATIENT_ID4);

        Mockito.when(patientService.getPatientIdsOfSamples(updatedSampleIds)).thenReturn(updatedPatientIds);

        List<ClinicalData> patientClinicalDataList = new ArrayList<>();
        ClinicalData patientClinicalData1 = new ClinicalData();
        patientClinicalData1.setAttrId(CLINICAL_ATTRIBUTE_ID_2);
        patientClinicalData1.setAttrValue("value2");
        patientClinicalData1.setPatientId(PATIENT_ID1);
        patientClinicalDataList.add(patientClinicalData1);
        ClinicalData patientClinicalData2 = new ClinicalData();
        patientClinicalData2.setAttrId(CLINICAL_ATTRIBUTE_ID_2);
        patientClinicalData2.setAttrValue("N/A");
        patientClinicalData2.setPatientId(PATIENT_ID2);
        patientClinicalDataList.add(patientClinicalData2);
        ClinicalData patientClinicalData3 = new ClinicalData();
        patientClinicalData3.setAttrId(CLINICAL_ATTRIBUTE_ID_2);
        patientClinicalData3.setAttrValue("value3");
        patientClinicalData3.setPatientId(PATIENT_ID3);
        patientClinicalDataList.add(patientClinicalData3);

        Mockito.when(clinicalDataService.fetchAllClinicalDataInStudy(STUDY_ID, updatedPatientIds, 
            Arrays.asList(CLINICAL_ATTRIBUTE_ID_2), "PATIENT", "SUMMARY")).thenReturn(patientClinicalDataList);

        Mockito.when(sampleService.getAllSamplesOfPatientsInStudy(STUDY_ID, updatedPatientIds, "ID")).thenReturn(updatedSamples);

        List<Mutation> mutations = new ArrayList<>();
        Mutation mutation1 = new Mutation();
        mutation1.setSampleId(SAMPLE_ID1);
        mutations.add(mutation1);
        Mutation mutation2 = new Mutation();
        mutation2.setSampleId(SAMPLE_ID2);
        mutations.add(mutation2);
        Mutation mutation3 = new Mutation();
        mutation3.setSampleId(SAMPLE_ID4);
        mutations.add(mutation3);
        Mutation mutation4 = new Mutation();
        mutation4.setSampleId(SAMPLE_ID4);
        mutations.add(mutation4);

        Mockito.when(mutationService.fetchMutationsInMolecularProfile(MOLECULAR_PROFILE_ID_1, updatedSampleIds, 
            Arrays.asList(ENTREZ_GENE_ID_1), null, "ID", null, null, null, null)).thenReturn(mutations);

        updatedSampleIds = new ArrayList<>();
        updatedSampleIds.add(SAMPLE_ID1);
        updatedSampleIds.add(SAMPLE_ID2);
        updatedSampleIds.add(SAMPLE_ID4);

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = new ArrayList<>();
        DiscreteCopyNumberData discreteCopyNumberData1 = new DiscreteCopyNumberData();
        discreteCopyNumberData1.setSampleId(SAMPLE_ID1);
        discreteCopyNumberDataList.add(discreteCopyNumberData1);
        DiscreteCopyNumberData discreteCopyNumberData2 = new DiscreteCopyNumberData();
        discreteCopyNumberData2.setSampleId(SAMPLE_ID1);
        discreteCopyNumberDataList.add(discreteCopyNumberData2);
        DiscreteCopyNumberData discreteCopyNumberData3 = new DiscreteCopyNumberData();
        discreteCopyNumberData3.setSampleId(SAMPLE_ID2);
        discreteCopyNumberDataList.add(discreteCopyNumberData3);

        Mockito.when(discreteCopyNumberService.fetchDiscreteCopyNumbersInMolecularProfile(MOLECULAR_PROFILE_ID_2, 
            updatedSampleIds, Arrays.asList(ENTREZ_GENE_ID_2), Arrays.asList(-2), "ID")).thenReturn(discreteCopyNumberDataList);
        
        List<String> result = studyViewFilterApplier.apply(STUDY_ID, studyViewFilter);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(SAMPLE_ID1, result.get(0));
        Assert.assertEquals(SAMPLE_ID2, result.get(1));
    }
}
