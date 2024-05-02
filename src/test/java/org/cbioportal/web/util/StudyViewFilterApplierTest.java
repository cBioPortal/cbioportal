package org.cbioportal.web.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.CNA;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneFilter;
import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.GenericAssayData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.Patient;
import org.cbioportal.model.Sample;
import org.cbioportal.model.util.Select;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GenericAssayService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.StructuralVariantService;
import org.cbioportal.service.impl.CustomDataServiceImpl;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.GeneIdType;
import org.cbioportal.web.parameter.GenericAssayDataFilter;
import org.cbioportal.web.parameter.MutationDataFilter;
import org.cbioportal.web.parameter.MutationOption;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
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
    public static final String CUSTOM_DATASET_ID = "custom_dataset_id";
    public static final Integer ENTREZ_GENE_ID_1 = 1;
    public static final Integer ENTREZ_GENE_ID_2 = 2;
    public static final String HUGO_GENE_SYMBOL_1 = "HUGO_GENE_SYMBOL_1";
    public static final String HUGO_GENE_SYMBOL_2 = "HUGO_GENE_SYMBOL_2";
    public static final String MOLECULAR_PROFILE_ID_1 = "molecular_profile_id1";
    public static final String MOLECULAR_PROFILE_ID_2 = "molecular_profile_id2";
    public static final String MUTATION_TYPE_1 = "mutation_type_1";
    public static final String MUTATION_TYPE_2 = "mutation_type_2";

    @InjectMocks
    private StudyViewFilterApplier studyViewFilterApplier;
    
    @Mock
    private ApplicationContext applicationContext;

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
    @Mock
    private StructuralVariantService structuralVariantService;
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
    @Spy
    @InjectMocks
    private MolecularProfileUtil molecularProfileUtil;

    @Mock
    private SessionServiceRequestHandler sessionServiceRequestHandler;
    @Spy
    private ObjectMapper sessionServiceObjectMapper = new ObjectMapper();

    @Spy
    @InjectMocks
    private CustomDataServiceImpl customDataService;

    @Spy
    @InjectMocks
    private CustomDataFilterApplier customDataFilterApplier;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(applicationContext.getBean(StudyViewFilterApplier.class)).thenReturn(studyViewFilterApplier);
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

        boolean includeDriver = true;
        boolean includeVUS = true;
        boolean includeUnknownOncogenicity = true;
        boolean includeGermline = true;
        boolean includeSomatic = true;
        boolean includeUnknownStatus = true;
        Select<String> selectedTiers = Select.none();
        boolean includeUnknownTier = true;
        List<GeneFilter> geneFilters = new ArrayList<>();
        GeneFilter mutationGeneFilter = new GeneFilter();
        mutationGeneFilter.setMolecularProfileIds(new HashSet<>(Arrays.asList(MOLECULAR_PROFILE_ID_1)));

        GeneFilterQuery geneFilterQuery1 = new GeneFilterQuery("HUGO_GENE_SYMBOL_1", null,
            null, includeDriver, includeVUS, includeUnknownOncogenicity,  selectedTiers, includeUnknownTier,
            includeGermline, includeSomatic, includeUnknownStatus);
        List<List<GeneFilterQuery>> q1 = new ArrayList<>();
        List<GeneFilterQuery> q2 = new ArrayList<>();
        q2.add(geneFilterQuery1);
        q1.add(q2);
        mutationGeneFilter.setGeneQueries(q1);

        GeneFilter copyNumberGeneFilter = new GeneFilter();
        copyNumberGeneFilter.setMolecularProfileIds(new HashSet<>(Arrays.asList(MOLECULAR_PROFILE_ID_2)));
        GeneFilterQuery geneFilterQuery2 = new GeneFilterQuery("HUGO_GENE_SYMBOL_2", null,
            Arrays.asList(CNA.HOMDEL), includeDriver, includeVUS, includeUnknownOncogenicity,  selectedTiers,
            includeUnknownTier,includeGermline, includeSomatic, includeUnknownStatus);
        List<List<GeneFilterQuery>> q3 = new ArrayList<>();
        List<GeneFilterQuery> q4 = new ArrayList<>();
        q4.add(geneFilterQuery2);
        q3.add(q4);
        copyNumberGeneFilter.setGeneQueries(q3);

        geneFilters.add(mutationGeneFilter);
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

        when(sampleService.fetchSamples(studyIds, sampleIds, "ID")).thenReturn(samples);
        when(patientService.getPatientsOfSamples(studyIds, sampleIds)).thenReturn(patients);

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

        when(clinicalDataService.getPatientClinicalDataDetailedToSample(
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

        when(sampleService.getSamplesOfPatientsInMultipleStudies(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID),
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

        when(
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

        when(
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

        when(molecularProfileService.getMolecularProfilesInStudies(Arrays.asList(STUDY_ID), "SUMMARY"))
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
        gene1.setHugoGeneSymbol(HUGO_GENE_SYMBOL_1);
        when(geneService.fetchGenes(Arrays.asList(HUGO_GENE_SYMBOL_1), GeneIdType.HUGO_GENE_SYMBOL.name(),
                Projection.SUMMARY.name())).thenReturn(Arrays.asList(gene1));
        when(mutationService.getMutationsInMultipleMolecularProfilesByGeneQueries(
            anyList(), anyList(), anyList(), anyString(), isNull(), isNull(), isNull(), isNull())).thenReturn(mutations);

        updatedSampleIds = new ArrayList<>();
        updatedSampleIds.add(SAMPLE_ID1);
        updatedSampleIds.add(SAMPLE_ID2);
        updatedSampleIds.add(SAMPLE_ID4);

        List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers = new ArrayList<>();
        MolecularProfileCaseIdentifier profileCaseIdentifier1 = new MolecularProfileCaseIdentifier();
        profileCaseIdentifier1.setCaseId(SAMPLE_ID1);
        profileCaseIdentifier1.setMolecularProfileId(MOLECULAR_PROFILE_ID_2);
        molecularProfileCaseIdentifiers.add(profileCaseIdentifier1);
        MolecularProfileCaseIdentifier profileCaseIdentifier2 = new MolecularProfileCaseIdentifier();
        profileCaseIdentifier2.setCaseId(SAMPLE_ID2);
        profileCaseIdentifier2.setMolecularProfileId(MOLECULAR_PROFILE_ID_2);
        molecularProfileCaseIdentifiers.add(profileCaseIdentifier2);
        MolecularProfileCaseIdentifier profileCaseIdentifier3 = new MolecularProfileCaseIdentifier();
        profileCaseIdentifier3.setCaseId(SAMPLE_ID4);
        profileCaseIdentifier3.setMolecularProfileId(MOLECULAR_PROFILE_ID_2);
        molecularProfileCaseIdentifiers.add(profileCaseIdentifier3);

        when(molecularProfileService.getFirstDiscreteCNAProfileCaseIdentifiers(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID),
                updatedSampleIds))
                .thenReturn(molecularProfileCaseIdentifiers);

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
        gene2.setHugoGeneSymbol(HUGO_GENE_SYMBOL_2);
        when(geneService.fetchGenes(Arrays.asList(HUGO_GENE_SYMBOL_2), GeneIdType.HUGO_GENE_SYMBOL.name(),
            Projection.SUMMARY.name())).thenReturn(Arrays.asList(gene2));

        when(discreteCopyNumberService.getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(
            anyList(), anyList(), anyList(), anyString())).thenReturn(discreteCopyNumberDataList);

        List<ClinicalAttribute> clinicalAttributeList = new ArrayList<>();
        ClinicalAttribute clinicalAttribute1 = new ClinicalAttribute();
        clinicalAttribute1.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        clinicalAttribute1.setDatatype("STRING");
        clinicalAttributeList.add(clinicalAttribute1);
        ClinicalAttribute clinicalAttribute2 = new ClinicalAttribute();
        clinicalAttribute2.setAttrId(CLINICAL_ATTRIBUTE_ID_2);
        clinicalAttribute2.setDatatype("STRING");
        clinicalAttributeList.add(clinicalAttribute2);

        when(clinicalAttributeService.getClinicalAttributesByStudyIdsAndAttributeIds(Arrays.asList(STUDY_ID),
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

        when(sampleService.fetchSamples(studyIds, sampleIds, "ID")).thenReturn(samples);
        when(patientService.getPatientsOfSamples(studyIds, sampleIds)).thenReturn(patients);

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

        when(
                clinicalDataService.fetchClinicalData(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID, STUDY_ID),
                        sampleIds, Arrays.asList(CLINICAL_ATTRIBUTE_ID_3), "SAMPLE", "SUMMARY"))
                .thenReturn(sampleClinicalDataList);

        when(clinicalDataService.getPatientClinicalDataDetailedToSample(
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

        when(clinicalAttributeService.getClinicalAttributesByStudyIdsAndAttributeIds(Arrays.asList(STUDY_ID),
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

    @Test
    public void applyPatientLevelGenericAssayFilter() throws Exception {

        List<String> studyIds = Collections.nCopies(5, STUDY_ID);

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

        when(sampleService.fetchSamples(studyIds, sampleIds, "ID")).thenReturn(samples);
        when(patientService.getPatientsOfSamples(studyIds, sampleIds)).thenReturn(patients);
        
        List<MolecularProfile> molecularProfiles = new ArrayList<>();
        MolecularProfile molecularProfile1 = new MolecularProfile();
        molecularProfile1.setStableId(MOLECULAR_PROFILE_ID_1);
        molecularProfile1.setCancerStudyIdentifier(STUDY_ID);
        molecularProfile1.setPatientLevel(true);
        molecularProfiles.add(molecularProfile1);
        
        when(molecularProfileService.getMolecularProfilesInStudies(Arrays.asList(STUDY_ID), "SUMMARY")).thenReturn(molecularProfiles);

        List<GenericAssayData> genericAssayDataList = new ArrayList<>();
        
        GenericAssayData genericAssayData1 = new GenericAssayData();
        genericAssayData1.setGenericAssayStableId(CLINICAL_ATTRIBUTE_ID_3);
        genericAssayData1.setMolecularProfileId(MOLECULAR_PROFILE_ID_1);
        genericAssayData1.setSampleId(SAMPLE_ID1);
        genericAssayData1.setPatientId(PATIENT_ID1);
        genericAssayData1.setStudyId(STUDY_ID);
        genericAssayData1.setValue("100");
        genericAssayDataList.add(genericAssayData1);

        GenericAssayData genericAssayData2 = new GenericAssayData();
        genericAssayData2.setGenericAssayStableId(CLINICAL_ATTRIBUTE_ID_3);
        genericAssayData2.setMolecularProfileId(MOLECULAR_PROFILE_ID_1);
        genericAssayData2.setSampleId(SAMPLE_ID2);
        genericAssayData2.setPatientId(PATIENT_ID1);
        genericAssayData2.setStudyId(STUDY_ID);
        genericAssayData2.setValue("100");
        genericAssayDataList.add(genericAssayData2);

        GenericAssayData genericAssayData3 = new GenericAssayData();
        genericAssayData3.setGenericAssayStableId(CLINICAL_ATTRIBUTE_ID_3);
        genericAssayData3.setMolecularProfileId(MOLECULAR_PROFILE_ID_1);
        genericAssayData3.setSampleId(SAMPLE_ID3);
        genericAssayData3.setPatientId(PATIENT_ID2);
        genericAssayData3.setStudyId(STUDY_ID);
        genericAssayData3.setValue("100");
        genericAssayDataList.add(genericAssayData3);

        GenericAssayData genericAssayData4 = new GenericAssayData();
        genericAssayData4.setGenericAssayStableId(CLINICAL_ATTRIBUTE_ID_3);
        genericAssayData4.setMolecularProfileId(MOLECULAR_PROFILE_ID_1);
        genericAssayData4.setSampleId(SAMPLE_ID4);
        genericAssayData4.setPatientId(PATIENT_ID3);
        genericAssayData4.setStudyId(STUDY_ID);
        genericAssayData4.setValue("100");
        genericAssayDataList.add(genericAssayData4);

        GenericAssayData genericAssayData5 = new GenericAssayData();
        genericAssayData5.setGenericAssayStableId(CLINICAL_ATTRIBUTE_ID_3);
        genericAssayData5.setMolecularProfileId(MOLECULAR_PROFILE_ID_1);
        genericAssayData5.setSampleId(SAMPLE_ID5);
        genericAssayData5.setPatientId(PATIENT_ID4);
        genericAssayData5.setStudyId(STUDY_ID);
        genericAssayData5.setValue("100");
        genericAssayDataList.add(genericAssayData5);

        when(genericAssayService
            .fetchGenericAssayData(Arrays.asList(MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_1, MOLECULAR_PROFILE_ID_1), sampleIds, Arrays.asList(CLINICAL_ATTRIBUTE_ID_3), "SUMMARY")).thenReturn(genericAssayDataList);
        
        GenericAssayDataFilter genericAssayDataFilter = new GenericAssayDataFilter();
        genericAssayDataFilter.setStableId(CLINICAL_ATTRIBUTE_ID_3);
        genericAssayDataFilter.setProfileType(MOLECULAR_PROFILE_ID_1);

        DataFilterValue filterValue1 = new DataFilterValue();
        filterValue1.setStart(new BigDecimal("50"));
        filterValue1.setEnd(new BigDecimal("150"));
        genericAssayDataFilter.setValues(Arrays.asList(filterValue1));
        studyViewFilter.setGenericAssayDataFilters(Arrays.asList(genericAssayDataFilter));

        List<SampleIdentifier> result = studyViewFilterApplier.apply(studyViewFilter);
        // Return 4 samples since this is a patient level profile
        // And sample1 sample2 belong to patient1
        Assert.assertEquals(4, result.size());
    }
    
    @Test
    public void applyNumericalCustomDataFilter() throws Exception {
        // Create samples:
        List<SampleIdentifier> sampleIdentifiers = new ArrayList<>();
        sampleIdentifiers.add(createSampleIdentifier(SAMPLE_ID1));
        sampleIdentifiers.add(createSampleIdentifier(SAMPLE_ID2));
        sampleIdentifiers.add(createSampleIdentifier(SAMPLE_ID3));
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setSampleIdentifiers(sampleIdentifiers);
        List<String> sampleIds = sampleIdentifiers
            .stream()
            .map(SampleIdentifier::getSampleId)
            .collect(toList());
        List<String> studyIds = sampleIdentifiers
            .stream()
            .map(SampleIdentifier::getStudyId)
            .collect(toList());
        List<Sample> samples = sampleIdentifiers
            .stream()
            .map(si -> createSample(si.getSampleId()))
            .collect(toList());

        // Create custom dataset interval filter: sample value must be between 0 and 10
        ClinicalDataFilter customDataFilter = new ClinicalDataFilter();
        customDataFilter.setAttributeId(CUSTOM_DATASET_ID);
        DataFilterValue intervalFilter = new DataFilterValue();
        intervalFilter.setStart(new BigDecimal("0"));
        intervalFilter.setEnd(new BigDecimal("10"));
        customDataFilter.setValues(of(intervalFilter));
        List<ClinicalDataFilter> customDataFilters = new ArrayList<>();
        customDataFilters.add(customDataFilter);
        studyViewFilter.setCustomDataFilters(customDataFilters);
        
        // Mock sample service:
        when(sampleService.fetchSamples(eq(studyIds), eq(sampleIds), eq("ID"))).thenReturn(samples);
        
        // Load custom dataset:
        String customDataset = getFileContents("classpath:numerical-custom-dataset-filter-applier.json");
        mockCustomDataService(customDataset);

        List<SampleIdentifier> result = studyViewFilterApplier.apply(studyViewFilter);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void applyCategoricalCustomDataFilter() throws Exception {
        // Create samples:
        List<SampleIdentifier> sampleIdentifiers = new ArrayList<>();
        sampleIdentifiers.add(createSampleIdentifier(SAMPLE_ID1));
        sampleIdentifiers.add(createSampleIdentifier(SAMPLE_ID2));
        sampleIdentifiers.add(createSampleIdentifier(SAMPLE_ID3));
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setSampleIdentifiers(sampleIdentifiers);
        List<String> sampleIds = sampleIdentifiers
            .stream()
            .map(SampleIdentifier::getSampleId)
            .collect(toList());
        List<String> studyIds = sampleIdentifiers
            .stream()
            .map(SampleIdentifier::getStudyId)
            .collect(toList());
        List<Sample> samples = sampleIdentifiers
            .stream()
            .map(si -> createSample(si.getSampleId()))
            .collect(toList());
        
        // Create custom dataset equality filter: sample value must be value2
        ClinicalDataFilter customDataFilter = new ClinicalDataFilter();
        customDataFilter.setAttributeId(CUSTOM_DATASET_ID);
        customDataFilter.setValues(of(createDataFilterValue("value2")));
        List<ClinicalDataFilter> customDataFilters = new ArrayList<>();
        customDataFilters.add(customDataFilter);
        studyViewFilter.setCustomDataFilters(customDataFilters);
        
        // Mock sample service:
        when(sampleService.fetchSamples(eq(studyIds), eq(sampleIds), eq("ID"))).thenReturn(samples);
        
        // Load custom dataset:
        String customDataset = getFileContents("classpath:categorical-custom-dataset-filter-applier.json");
        mockCustomDataService(customDataset);

        List<SampleIdentifier> result = studyViewFilterApplier.apply(studyViewFilter);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void applyMutationDataFilter() throws Exception {

        List<String> studyIds = Collections.nCopies(5, STUDY_ID);

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

        when(sampleService.fetchSamples(studyIds, sampleIds, "ID")).thenReturn(samples);

        List<MolecularProfile> molecularProfiles = new ArrayList<>();
        MolecularProfile molecularProfile1 = new MolecularProfile();
        molecularProfile1.setStableId(MOLECULAR_PROFILE_ID_1);
        molecularProfile1.setCancerStudyIdentifier(STUDY_ID);
        molecularProfiles.add(molecularProfile1);

        when(molecularProfileService.getMolecularProfilesInStudies(List.of(STUDY_ID), "SUMMARY")).thenReturn(molecularProfiles);

        List<Mutation> mutationList = new ArrayList<>();
        
        Mutation mutation1 = new Mutation();
        mutation1.setSampleId(SAMPLE_ID1);
        mutation1.setPatientId(PATIENT_ID1);
        mutation1.setStudyId(STUDY_ID);
        mutation1.setMutationType(MUTATION_TYPE_1);
        mutationList.add(mutation1);

        Mutation mutation2 = new Mutation();
        mutation2.setSampleId(SAMPLE_ID2);
        mutation2.setPatientId(PATIENT_ID1);
        mutation2.setStudyId(STUDY_ID);
        mutation2.setMutationType(MUTATION_TYPE_1);
        mutationList.add(mutation2);

        Mutation mutation3 = new Mutation();
        mutation3.setSampleId(SAMPLE_ID3);
        mutation3.setPatientId(PATIENT_ID2);
        mutation3.setStudyId(STUDY_ID);
        mutation3.setMutationType(MUTATION_TYPE_2);
        mutationList.add(mutation3);

        Mutation mutation4 = new Mutation();
        mutation4.setSampleId(SAMPLE_ID4);
        mutation4.setPatientId(PATIENT_ID2);
        mutation4.setStudyId(STUDY_ID);
        mutation4.setMutationType(MUTATION_TYPE_2);
        mutationList.add(mutation4);

        Gene gene1 = new Gene();
        gene1.setEntrezGeneId(ENTREZ_GENE_ID_1);
        gene1.setHugoGeneSymbol(HUGO_GENE_SYMBOL_1);
        
        when(geneService.fetchGenes(Arrays.asList(HUGO_GENE_SYMBOL_1), GeneIdType.HUGO_GENE_SYMBOL.name(),
            Projection.SUMMARY.name())).thenReturn(Arrays.asList(gene1));
        when(mutationService.getMutationsInMultipleMolecularProfiles(anyList(), anyList(),
            anyList(), anyString(),
            isNull(), isNull(), isNull(), isNull())).thenReturn(mutationList);

        MutationDataFilter mutationDataFilter = new MutationDataFilter();
        mutationDataFilter.setHugoGeneSymbol(HUGO_GENE_SYMBOL_1);
        mutationDataFilter.setProfileType(MOLECULAR_PROFILE_ID_1);
        mutationDataFilter.setCategorization(MutationOption.MUTATED);

        DataFilterValue filterValue1 = new DataFilterValue();
        filterValue1.setValue("MUTATED");
        mutationDataFilter.setValues(List.of(List.of(filterValue1)));
        studyViewFilter.setMutationDataFilters(Arrays.asList(mutationDataFilter));

        List<SampleIdentifier> result1 = studyViewFilterApplier.apply(studyViewFilter);
        // Return 4 samples since four mutations are MUTATED
        Assert.assertEquals(4, result1.size());
        
        DataFilterValue filterValue2 = new DataFilterValue();
        filterValue2.setValue(MUTATION_TYPE_1);
        mutationDataFilter.setCategorization(MutationOption.MUTATION_TYPE);
        mutationDataFilter.setValues(List.of(List.of(filterValue2)));
        studyViewFilter.setMutationDataFilters(Collections.singletonList(mutationDataFilter));
        
        // Return 2 samples since two mutations are MUTATION_TYPE_1
        List<SampleIdentifier> result2 = studyViewFilterApplier.apply(studyViewFilter);
        Assert.assertEquals(2, result2.size());
    }

    private DataFilterValue createDataFilterValue(String value) {
        DataFilterValue equalityFilter = new DataFilterValue();
        equalityFilter.setValue(value);
        return equalityFilter;
    }

    private Sample createSample(String sampleId) {
        Sample sample1 = new Sample();
        sample1.setStableId(sampleId);
        sample1.setCancerStudyIdentifier(STUDY_ID);
        return sample1;
    }

    private SampleIdentifier createSampleIdentifier(String sampleId) {
        SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
        sampleIdentifier1.setSampleId(sampleId);
        sampleIdentifier1.setStudyId(STUDY_ID);
        return sampleIdentifier1;
    }

    private void mockCustomDataService(String customDatasetFile) throws Exception {
        when(
            sessionServiceRequestHandler.getSessionDataJson(any(), any())
        ).thenReturn(customDatasetFile);
    }
    
    private String getFileContents(String resourceLocation) throws IOException {
        return new String(Files.readAllBytes(ResourceUtils.getFile(resourceLocation).toPath()));
    }
    
}
