package org.cbioportal.web.util;

import java.math.BigDecimal;
import java.util.*;
import org.cbioportal.model.*;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.service.*;
import org.cbioportal.web.parameter.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
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
    public static final String CLINICAL_ATTRIBUTE_ID_3 = "attribute_id3";
    public static final Integer ENTREZ_GENE_ID_1 = 1;
    public static final Integer ENTREZ_GENE_ID_2 = 2;
    public static final String HUGO_GENE_SYMBOL_1 = "HUGO_GENE_SYMBOL_1";
    public static final String HUGO_GENE_SYMBOL_2 = "HUGO_GENE_SYMBOL_2";
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
    @Mock
    private MolecularProfileService molecularProfileService;
    @Mock
    private GenePanelService genePanelService;
    @Mock
    private GeneService geneService;
    @Mock
    private ClinicalAttributeService clinicalAttributeService;
    @Mock
    private MolecularDataService molecularDataService;
    @Mock
    private SampleListService sampleListService;
    @Mock
    private GenericAssayService genericAssayService;
    // Do not mock utility classes, we also want to test their functionality
    @Spy
    @InjectMocks
    private ClinicalDataEqualityFilterApplier clinicalDataEqualityFilterApplier;
    @Spy
    @InjectMocks
    private ClinicalDataIntervalFilterApplier clinicalDataIntervalFilterApplier;
    @Spy
    @InjectMocks
    private StudyViewFilterUtil studyViewFilterUtil;
    @Spy
    @InjectMocks
    private DataBinner dataBinner;
    @Spy
    @InjectMocks
    private DataBinHelper dataBinHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

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

        List<SampleIdentifier> sampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
        sampleIdentifier1.setSampleId(SAMPLE_ID1);
        sampleIdentifier1.setStudyId(STUDY_ID);
        sampleIdentifiers.add(sampleIdentifier1);
        SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
        sampleIdentifier2.setSampleId(SAMPLE_ID2);
        sampleIdentifier2.setStudyId(STUDY_ID);
        sampleIdentifiers.add(sampleIdentifier2);
        SampleIdentifier sampleIdentifier3 = new SampleIdentifier();
        sampleIdentifier3.setSampleId(SAMPLE_ID3);
        sampleIdentifier3.setStudyId(STUDY_ID);
        sampleIdentifiers.add(sampleIdentifier3);
        SampleIdentifier sampleIdentifier4 = new SampleIdentifier();
        sampleIdentifier4.setSampleId(SAMPLE_ID4);
        sampleIdentifier4.setStudyId(STUDY_ID);
        sampleIdentifiers.add(sampleIdentifier4);
        SampleIdentifier sampleIdentifier5 = new SampleIdentifier();
        sampleIdentifier5.setSampleId(SAMPLE_ID5);
        sampleIdentifier5.setStudyId(STUDY_ID);
        sampleIdentifiers.add(sampleIdentifier5);
        studyViewFilter.setSampleIdentifiers(sampleIdentifiers);
        List<ClinicalDataFilter> clinicalDataEqualityFilters = new ArrayList<>();
        ClinicalDataFilter clinicalDataEqualityFilter1 = new ClinicalDataFilter();
        clinicalDataEqualityFilter1.setAttributeId(CLINICAL_ATTRIBUTE_ID_1);
        DataFilterValue filterValue = new DataFilterValue();
        filterValue.setValue("value1");
        clinicalDataEqualityFilter1.setValues(Arrays.asList(filterValue));
        clinicalDataEqualityFilters.add(clinicalDataEqualityFilter1);
        ClinicalDataFilter clinicalDataEqualityFilter2 = new ClinicalDataFilter();
        clinicalDataEqualityFilter2.setAttributeId(CLINICAL_ATTRIBUTE_ID_2);

        DataFilterValue filterValue1 = new DataFilterValue();
        filterValue1.setValue("value1");
        DataFilterValue filterValue2 = new DataFilterValue();
        filterValue2.setValue("NA");
        clinicalDataEqualityFilter2.setValues(Arrays.asList(filterValue1, filterValue2));
        clinicalDataEqualityFilters.add(clinicalDataEqualityFilter2);
        studyViewFilter.setClinicalDataFilters(clinicalDataEqualityFilters);
        List<GeneFilter> geneFilters = new ArrayList<>();
        GeneFilter mutationGeneFilter = new GeneFilter();
        mutationGeneFilter.setGeneQueries(Arrays.asList(Arrays.asList(HUGO_GENE_SYMBOL_1)));
        mutationGeneFilter.setMolecularProfileIds(new HashSet<>(Arrays.asList(MOLECULAR_PROFILE_ID_1)));
        geneFilters.add(mutationGeneFilter);

        GeneFilter copyNumberGeneFilter = new GeneFilter();
        copyNumberGeneFilter.setGeneQueries(Arrays.asList(Arrays.asList(HUGO_GENE_SYMBOL_2 + ":HOMDEL")));
        copyNumberGeneFilter.setMolecularProfileIds(new HashSet<>(Arrays.asList(MOLECULAR_PROFILE_ID_2)));
        geneFilters.add(copyNumberGeneFilter);
        studyViewFilter.setGeneFilters(geneFilters);

        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setStableId(SAMPLE_ID1);
        sample1.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setStableId(SAMPLE_ID2);
        sample2.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample2);
        Sample sample3 = new Sample();
        sample3.setStableId(SAMPLE_ID3);
        sample3.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample3);
        Sample sample4 = new Sample();
        sample4.setStableId(SAMPLE_ID4);
        sample4.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample4);
        Sample sample5 = new Sample();
        sample5.setStableId(SAMPLE_ID5);
        sample5.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample5);

        List<String> patientIds = new ArrayList<>();
        patientIds.add(PATIENT_ID1);
        patientIds.add(PATIENT_ID2);
        patientIds.add(PATIENT_ID3);
        patientIds.add(PATIENT_ID4);

        List<Patient> patients = new ArrayList<>();
        Patient patient1 = new Patient();
        patient1.setStableId(PATIENT_ID1);
        patient1.setCancerStudyIdentifier(STUDY_ID);
        patients.add(patient1);
        Patient patient2 = new Patient();
        patient2.setStableId(PATIENT_ID2);
        patient2.setCancerStudyIdentifier(STUDY_ID);
        patients.add(patient2);
        Patient patient3 = new Patient();
        patient3.setStableId(PATIENT_ID3);
        patient3.setCancerStudyIdentifier(STUDY_ID);
        patients.add(patient3);
        Patient patient4 = new Patient();
        patient4.setStableId(PATIENT_ID4);
        patient4.setCancerStudyIdentifier(STUDY_ID);
        patients.add(patient4);

        Mockito.when(sampleService.fetchSamples(studyIds, sampleIds, "ID")).thenReturn(samples);
        Mockito.when(patientService.getPatientsOfSamples(studyIds, sampleIds)).thenReturn(patients);

        List<ClinicalData> patientClinicalDataList = new ArrayList<>();
        ClinicalData patientClinicalData1 = new ClinicalData();
        patientClinicalData1.setAttrId(CLINICAL_ATTRIBUTE_ID_2);
        patientClinicalData1.setAttrValue("value2");
        patientClinicalData1.setPatientId(PATIENT_ID1);
        patientClinicalData1.setSampleId(SAMPLE_ID1);
        patientClinicalData1.setStudyId(STUDY_ID);
        patientClinicalDataList.add(patientClinicalData1);
        ClinicalData patientClinicalData2 = new ClinicalData();
        patientClinicalData2.setAttrId(CLINICAL_ATTRIBUTE_ID_2);
        patientClinicalData2.setAttrValue("N/A");
        patientClinicalData2.setPatientId(PATIENT_ID2);
        patientClinicalData2.setSampleId(SAMPLE_ID2);
        patientClinicalData2.setStudyId(STUDY_ID);
        patientClinicalDataList.add(patientClinicalData2);
        ClinicalData patientClinicalData3 = new ClinicalData();
        patientClinicalData3.setAttrId(CLINICAL_ATTRIBUTE_ID_2);
        patientClinicalData3.setAttrValue("value3");
        patientClinicalData3.setPatientId(PATIENT_ID3);
        patientClinicalData3.setSampleId(SAMPLE_ID3);
        patientClinicalData3.setStudyId(STUDY_ID);
        patientClinicalDataList.add(patientClinicalData3);

        Mockito.when(clinicalDataService.getPatientClinicalDataDetailedToSample(
                Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID), patientIds,
                Arrays.asList(CLINICAL_ATTRIBUTE_ID_1, CLINICAL_ATTRIBUTE_ID_2))).thenReturn(patientClinicalDataList);

        List<String> updatedPatientIds = new ArrayList<>();
        updatedPatientIds.add(PATIENT_ID1);
        updatedPatientIds.add(PATIENT_ID2);
        updatedPatientIds.add(PATIENT_ID4);

        List<Sample> updatedSamples = new ArrayList<>();
        updatedSamples.add(sample1);
        updatedSamples.add(sample2);
        updatedSamples.add(sample4);
        updatedSamples.add(sample5);

        Mockito.when(sampleService.getSamplesOfPatientsInMultipleStudies(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID),
                updatedPatientIds, "ID")).thenReturn(updatedSamples);

        List<ClinicalData> sampleClinicalDataList = new ArrayList<>();
        ClinicalData sampleClinicalData1 = new ClinicalData();
        sampleClinicalData1.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalData1.setAttrValue("value1");
        sampleClinicalData1.setSampleId(SAMPLE_ID1);
        sampleClinicalData1.setStudyId(STUDY_ID);
        sampleClinicalDataList.add(sampleClinicalData1);
        ClinicalData sampleClinicalData2 = new ClinicalData();
        sampleClinicalData2.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalData2.setAttrValue("value1");
        sampleClinicalData2.setSampleId(SAMPLE_ID2);
        sampleClinicalData2.setStudyId(STUDY_ID);
        sampleClinicalDataList.add(sampleClinicalData2);
        ClinicalData sampleClinicalData3 = new ClinicalData();
        sampleClinicalData3.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalData3.setAttrValue("NAN");
        sampleClinicalData3.setSampleId(SAMPLE_ID3);
        sampleClinicalData3.setStudyId(STUDY_ID);
        sampleClinicalDataList.add(sampleClinicalData3);
        ClinicalData sampleClinicalData4 = new ClinicalData();
        sampleClinicalData4.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalData4.setAttrValue("value1");
        sampleClinicalData4.setSampleId(SAMPLE_ID4);
        sampleClinicalData4.setStudyId(STUDY_ID);
        sampleClinicalDataList.add(sampleClinicalData4);
        ClinicalData sampleClinicalData5 = new ClinicalData();
        sampleClinicalData5.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalData5.setAttrValue("value1");
        sampleClinicalData5.setSampleId(SAMPLE_ID5);
        sampleClinicalData5.setStudyId(STUDY_ID);
        sampleClinicalDataList.add(sampleClinicalData5);

        List<String> updatedSampleIds = new ArrayList<>();
        updatedSampleIds.add(SAMPLE_ID1);
        updatedSampleIds.add(SAMPLE_ID2);
        updatedSampleIds.add(SAMPLE_ID4);
        updatedSampleIds.add(SAMPLE_ID5);

        Mockito.when(
                clinicalDataService.fetchClinicalData(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
                        Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3, SAMPLE_ID4, SAMPLE_ID5),
                        Arrays.asList(CLINICAL_ATTRIBUTE_ID_1, CLINICAL_ATTRIBUTE_ID_2), "SAMPLE", "SUMMARY"))
                .thenReturn(sampleClinicalDataList);

        List<Patient> updatedPatients = new ArrayList<>();
        Patient updatedPatient1 = new Patient();
        updatedPatient1.setStableId(PATIENT_ID1);
        updatedPatient1.setCancerStudyIdentifier(STUDY_ID);
        updatedPatients.add(updatedPatient1);
        Patient updatedPatient2 = new Patient();
        updatedPatient2.setStableId(PATIENT_ID2);
        updatedPatient2.setCancerStudyIdentifier(STUDY_ID);
        updatedPatients.add(updatedPatient2);
        Patient updatedPatient3 = new Patient();
        updatedPatient3.setStableId(PATIENT_ID4);
        updatedPatient3.setCancerStudyIdentifier(STUDY_ID);
        updatedPatients.add(updatedPatient3);

        Mockito.when(
                patientService.getPatientsOfSamples(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
                        Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3, SAMPLE_ID4, SAMPLE_ID5)))
                .thenReturn(updatedPatients);

        MolecularProfile molecularProfile1 = new MolecularProfile();
        molecularProfile1.setStableId(MOLECULAR_PROFILE_ID_1);
        molecularProfile1.setMolecularAlterationType(MolecularAlterationType.MUTATION_EXTENDED);
        molecularProfile1.setCancerStudyIdentifier(STUDY_ID);

        MolecularProfile molecularProfile2 = new MolecularProfile();
        molecularProfile2.setStableId(MOLECULAR_PROFILE_ID_2);
        molecularProfile2.setCancerStudyIdentifier(STUDY_ID);
        molecularProfile2.setMolecularAlterationType(MolecularAlterationType.COPY_NUMBER_ALTERATION);
        molecularProfile2.setDatatype("DISCRETE");

        Mockito.when(molecularProfileService.getMolecularProfilesInStudies(Arrays.asList(STUDY_ID), "SUMMARY"))
                .thenReturn(Arrays.asList(molecularProfile1, molecularProfile2));

        List<Mutation> mutations = new ArrayList<>();
        Mutation mutation1 = new Mutation();
        mutation1.setSampleId(SAMPLE_ID1);
        mutation1.setStudyId(STUDY_ID);
        mutations.add(mutation1);
        Mutation mutation2 = new Mutation();
        mutation2.setSampleId(SAMPLE_ID2);
        mutation2.setStudyId(STUDY_ID);
        mutations.add(mutation2);
        Mutation mutation3 = new Mutation();
        mutation3.setSampleId(SAMPLE_ID4);
        mutation3.setStudyId(STUDY_ID);
        mutations.add(mutation3);
        Mutation mutation4 = new Mutation();
        mutation4.setSampleId(SAMPLE_ID4);
        mutation4.setStudyId(STUDY_ID);
        mutations.add(mutation4);

        Gene gene1 = new Gene();
        gene1.setEntrezGeneId(ENTREZ_GENE_ID_1);
        Mockito.when(geneService.fetchGenes(Arrays.asList(HUGO_GENE_SYMBOL_1), GeneIdType.HUGO_GENE_SYMBOL.name(),
                Projection.SUMMARY.name())).thenReturn(Arrays.asList(gene1));

        Mockito.when(mutationService.getMutationsInMultipleMolecularProfiles(
                Arrays.asList(MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_1,
                        MOLECULAR_PROFILE_ID_1),
                updatedSampleIds, Arrays.asList(ENTREZ_GENE_ID_1), "ID", null, null, null, null)).thenReturn(mutations);

        updatedSampleIds = new ArrayList<>();
        updatedSampleIds.add(SAMPLE_ID1);
        updatedSampleIds.add(SAMPLE_ID2);
        updatedSampleIds.add(SAMPLE_ID4);

        Mockito.when(molecularProfileService.getFirstDiscreteCNAProfileIds(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID),
                updatedSampleIds))
                .thenReturn(Arrays.asList(MOLECULAR_PROFILE_ID_2, MOLECULAR_PROFILE_ID_2, MOLECULAR_PROFILE_ID_2));

        List<DiscreteCopyNumberData> discreteCopyNumberDataList = new ArrayList<>();
        DiscreteCopyNumberData discreteCopyNumberData1 = new DiscreteCopyNumberData();
        discreteCopyNumberData1.setSampleId(SAMPLE_ID1);
        discreteCopyNumberData1.setStudyId(STUDY_ID);
        discreteCopyNumberDataList.add(discreteCopyNumberData1);
        DiscreteCopyNumberData discreteCopyNumberData2 = new DiscreteCopyNumberData();
        discreteCopyNumberData2.setSampleId(SAMPLE_ID1);
        discreteCopyNumberData2.setStudyId(STUDY_ID);
        discreteCopyNumberDataList.add(discreteCopyNumberData2);
        DiscreteCopyNumberData discreteCopyNumberData3 = new DiscreteCopyNumberData();
        discreteCopyNumberData3.setSampleId(SAMPLE_ID2);
        discreteCopyNumberData3.setStudyId(STUDY_ID);
        discreteCopyNumberDataList.add(discreteCopyNumberData3);

        Gene gene2 = new Gene();
        gene2.setEntrezGeneId(ENTREZ_GENE_ID_2);
        Mockito.when(geneService.fetchGenes(Arrays.asList(HUGO_GENE_SYMBOL_2), GeneIdType.HUGO_GENE_SYMBOL.name(),
                Projection.SUMMARY.name())).thenReturn(Arrays.asList(gene2));

        Mockito.when(discreteCopyNumberService.getDiscreteCopyNumbersInMultipleMolecularProfiles(
                Arrays.asList(MOLECULAR_PROFILE_ID_2, MOLECULAR_PROFILE_ID_2, MOLECULAR_PROFILE_ID_2), updatedSampleIds,
                Arrays.asList(ENTREZ_GENE_ID_2), Arrays.asList(-2), "ID")).thenReturn(discreteCopyNumberDataList);

        List<ClinicalAttribute> clinicalAttributeList = new ArrayList<>();
        ClinicalAttribute clinicalAttribute1 = new ClinicalAttribute();
        clinicalAttribute1.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        clinicalAttribute1.setDatatype("STRING");
        clinicalAttributeList.add(clinicalAttribute1);
        ClinicalAttribute clinicalAttribute2 = new ClinicalAttribute();
        clinicalAttribute2.setAttrId(CLINICAL_ATTRIBUTE_ID_2);
        clinicalAttribute2.setDatatype("STRING");
        clinicalAttributeList.add(clinicalAttribute2);

        Mockito.when(clinicalAttributeService.getClinicalAttributesByStudyIdsAndAttributeIds(Arrays.asList(STUDY_ID),
                Arrays.asList(CLINICAL_ATTRIBUTE_ID_1, CLINICAL_ATTRIBUTE_ID_2))).thenReturn(clinicalAttributeList);

        List<SampleIdentifier> result = studyViewFilterApplier.apply(studyViewFilter);

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(SAMPLE_ID1, result.get(0).getSampleId());
        Assert.assertEquals(SAMPLE_ID2, result.get(1).getSampleId());
    }

    @Test
    public void applyIntervalFilters() throws Exception {

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

        List<SampleIdentifier> sampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
        sampleIdentifier1.setSampleId(SAMPLE_ID1);
        sampleIdentifier1.setStudyId(STUDY_ID);
        sampleIdentifiers.add(sampleIdentifier1);
        SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
        sampleIdentifier2.setSampleId(SAMPLE_ID2);
        sampleIdentifier2.setStudyId(STUDY_ID);
        sampleIdentifiers.add(sampleIdentifier2);
        SampleIdentifier sampleIdentifier3 = new SampleIdentifier();
        sampleIdentifier3.setSampleId(SAMPLE_ID3);
        sampleIdentifier3.setStudyId(STUDY_ID);
        sampleIdentifiers.add(sampleIdentifier3);
        SampleIdentifier sampleIdentifier4 = new SampleIdentifier();
        sampleIdentifier4.setSampleId(SAMPLE_ID4);
        sampleIdentifier4.setStudyId(STUDY_ID);
        sampleIdentifiers.add(sampleIdentifier4);
        SampleIdentifier sampleIdentifier5 = new SampleIdentifier();
        sampleIdentifier5.setSampleId(SAMPLE_ID5);
        sampleIdentifier5.setStudyId(STUDY_ID);
        sampleIdentifiers.add(sampleIdentifier5);
        studyViewFilter.setSampleIdentifiers(sampleIdentifiers);

        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setStableId(SAMPLE_ID1);
        sample1.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setStableId(SAMPLE_ID2);
        sample2.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample2);
        Sample sample3 = new Sample();
        sample3.setStableId(SAMPLE_ID3);
        sample3.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample3);
        Sample sample4 = new Sample();
        sample4.setStableId(SAMPLE_ID4);
        sample4.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample4);
        Sample sample5 = new Sample();
        sample5.setStableId(SAMPLE_ID5);
        sample5.setCancerStudyIdentifier(STUDY_ID);
        samples.add(sample5);

        List<Patient> patients = new ArrayList<>();
        Patient patient1 = new Patient();
        patient1.setStableId(PATIENT_ID1);
        patient1.setCancerStudyIdentifier(STUDY_ID);
        patients.add(patient1);
        Patient patient2 = new Patient();
        patient2.setStableId(PATIENT_ID2);
        patient2.setCancerStudyIdentifier(STUDY_ID);
        patients.add(patient2);
        Patient patient3 = new Patient();
        patient3.setStableId(PATIENT_ID3);
        patient3.setCancerStudyIdentifier(STUDY_ID);
        patients.add(patient3);
        Patient patient4 = new Patient();
        patient4.setStableId(PATIENT_ID4);
        patient4.setCancerStudyIdentifier(STUDY_ID);
        patients.add(patient4);

        Mockito.when(sampleService.fetchSamples(studyIds, sampleIds, "ID")).thenReturn(samples);
        Mockito.when(patientService.getPatientsOfSamples(studyIds, sampleIds)).thenReturn(patients);

        List<ClinicalData> sampleClinicalDataList = new ArrayList<>();
        ClinicalData sampleClinicalData1 = new ClinicalData();
        sampleClinicalData1.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        sampleClinicalData1.setAttrValue("66.6");
        sampleClinicalData1.setSampleId(SAMPLE_ID1);
        sampleClinicalData1.setStudyId(STUDY_ID);
        sampleClinicalDataList.add(sampleClinicalData1);
        ClinicalData sampleClinicalData2 = new ClinicalData();
        sampleClinicalData2.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        sampleClinicalData2.setAttrValue("666");
        sampleClinicalData2.setSampleId(SAMPLE_ID2);
        sampleClinicalData2.setStudyId(STUDY_ID);
        sampleClinicalDataList.add(sampleClinicalData2);
        ClinicalData sampleClinicalData3 = new ClinicalData();
        sampleClinicalData3.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        sampleClinicalData3.setAttrValue("NAN");
        sampleClinicalData3.setSampleId(SAMPLE_ID3);
        sampleClinicalData3.setStudyId(STUDY_ID);
        sampleClinicalDataList.add(sampleClinicalData3);
        ClinicalData sampleClinicalData4 = new ClinicalData();
        sampleClinicalData4.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        sampleClinicalData4.setAttrValue("6.66");
        sampleClinicalData4.setSampleId(SAMPLE_ID4);
        sampleClinicalData4.setStudyId(STUDY_ID);
        sampleClinicalDataList.add(sampleClinicalData4);
        ClinicalData sampleClinicalData5 = new ClinicalData();
        sampleClinicalData5.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        sampleClinicalData5.setAttrValue("SOMETHING_ELSE");
        sampleClinicalData5.setSampleId(SAMPLE_ID5);
        sampleClinicalData5.setStudyId(STUDY_ID);
        sampleClinicalDataList.add(sampleClinicalData5);

        Mockito.when(
                clinicalDataService.fetchClinicalData(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
                        sampleIds, Arrays.asList(CLINICAL_ATTRIBUTE_ID_3), "SAMPLE", "SUMMARY"))
                .thenReturn(sampleClinicalDataList);

        Mockito.when(clinicalDataService.getPatientClinicalDataDetailedToSample(
                Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID), sampleIds,
                Arrays.asList(CLINICAL_ATTRIBUTE_ID_3))).thenReturn(new ArrayList<ClinicalData>());

        List<ClinicalDataFilter> clinicalDataIntervalFilters = new ArrayList<>();
        ClinicalDataFilter clinicalDataIntervalFilter1 = new ClinicalDataFilter();
        clinicalDataIntervalFilter1.setAttributeId(CLINICAL_ATTRIBUTE_ID_3);
        DataFilterValue filterValue1 = new DataFilterValue();
        filterValue1.setStart(new BigDecimal("66.6"));
        filterValue1.setEnd(new BigDecimal("666"));
        clinicalDataIntervalFilter1.setValues(Collections.singletonList(filterValue1));
        clinicalDataIntervalFilters.add(clinicalDataIntervalFilter1);
        studyViewFilter.setClinicalDataFilters(clinicalDataIntervalFilters);

        List<ClinicalAttribute> clinicalAttributeList = new ArrayList<>();
        ClinicalAttribute clinicalAttribute1 = new ClinicalAttribute();
        clinicalAttribute1.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        clinicalAttribute1.setDatatype("NUMBER");
        clinicalAttributeList.add(clinicalAttribute1);

        Mockito.when(clinicalAttributeService.getClinicalAttributesByStudyIdsAndAttributeIds(Arrays.asList(STUDY_ID),
                Arrays.asList(CLINICAL_ATTRIBUTE_ID_3))).thenReturn(clinicalAttributeList);

        List<SampleIdentifier> result1 = studyViewFilterApplier.apply(studyViewFilter);
        Assert.assertEquals(1, result1.size());

        DataFilterValue filterValue2 = new DataFilterValue();
        filterValue2.setStart(new BigDecimal("6.66"));
        filterValue2.setEnd(new BigDecimal("66.6"));
        clinicalDataIntervalFilter1.setValues(Arrays.asList(filterValue1, filterValue2));

        List<SampleIdentifier> result2 = studyViewFilterApplier.apply(studyViewFilter);
        Assert.assertEquals(2, result2.size());

        DataFilterValue filterValue3 = new DataFilterValue();
        filterValue3.setStart(new BigDecimal("6.66"));
        filterValue3.setEnd(new BigDecimal("666"));
        clinicalDataIntervalFilter1.setValues(Arrays.asList(filterValue1, filterValue2, filterValue3));

        List<SampleIdentifier> result3 = studyViewFilterApplier.apply(studyViewFilter);
        Assert.assertEquals(2, result3.size());

        DataFilterValue filterValue4 = new DataFilterValue();
        filterValue4.setValue("na");
        clinicalDataIntervalFilter1.setValues(Arrays.asList(filterValue3, filterValue4));

        List<SampleIdentifier> result4 = studyViewFilterApplier.apply(studyViewFilter);
        Assert.assertEquals(3, result4.size());

        DataFilterValue filterValue5 = new DataFilterValue();
        filterValue5.setValue("something_else");
        clinicalDataIntervalFilter1.setValues(Arrays.asList(filterValue1, filterValue5));

        List<SampleIdentifier> result5 = studyViewFilterApplier.apply(studyViewFilter);
        Assert.assertEquals(2, result5.size());
    }
}
