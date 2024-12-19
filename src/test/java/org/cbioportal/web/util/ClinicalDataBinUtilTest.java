package org.cbioportal.web.util;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.Patient;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.impl.CustomDataServiceImpl;
import org.cbioportal.service.util.ClinicalAttributeUtil;
import org.cbioportal.service.util.SessionServiceRequestHandler;
import org.cbioportal.web.parameter.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ClinicalDataBinUtilTest {
    private static String STUDY_ID = "genie_bpc_test";
    private static Integer STUDY_ID_INT = 205;
    
    @Spy
    @InjectMocks
    private ClinicalDataBinUtil clinicalDataBinUtil;
    @Mock
    private StudyViewFilterApplier studyViewFilterApplier;
    @Mock
    private ClinicalDataFetcher clinicalDataFetcher;
    @Mock
    private ClinicalAttributeService clinicalAttributeService;
    @Mock
    private PatientService patientService;
    @Mock
    private SessionServiceRequestHandler sessionServiceRequestHandler;
    @Spy
    private ObjectMapper sessionServiceObjectMapper = new ObjectMapper();
    @Spy
    @InjectMocks
    private CustomDataServiceImpl customDataService;
    @Spy
    private StudyViewFilterUtil studyViewFilterUtil;
    @Spy
    private ClinicalAttributeUtil clinicalAttributeUtil;
    
    @Spy
    @InjectMocks
    private DataBinner dataBinner;
    
    @Spy
    @InjectMocks
    private DiscreteDataBinner discreteDataBinner;
    @Spy
    @InjectMocks
    private LinearDataBinner linearDataBinner;
    @Spy
    @InjectMocks
    private ScientificSmallDataBinner scientificSmallDataBinner;

    @Spy
    @InjectMocks
    private IdPopulator idPopulator;
    
    @Spy
    @InjectMocks
    private LogScaleDataBinner logScaleDataBinner;
    @Spy
    private DataBinHelper dataBinHelper;
    private final String testDataAttributeId = "test";
    private final ObjectMapper customDatasetMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testUnfilteredFetchClinicalDataBinCounts() {
        mockUnfilteredQuery();
        
        List<ClinicalDataBin> dataBins = clinicalDataBinUtil.fetchClinicalDataBinCounts(
            DataBinMethod.STATIC,
            mockBaseFilter()
        );

        
        // assert data bin counts
        
        assertEquals(33, dataBins.size());
        
        List<ClinicalDataBin> mutationCountBins = 
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("MUTATION_COUNT")).collect(Collectors.toList());
        assertEquals(6, mutationCountBins.size());
        assertEquals(2, mutationCountBins.get(0).getCount().intValue());
        assertEquals(1, mutationCountBins.get(1).getCount().intValue());
        assertEquals(1, mutationCountBins.get(2).getCount().intValue());
        assertEquals(1, mutationCountBins.get(3).getCount().intValue());
        assertEquals(1, mutationCountBins.get(4).getCount().intValue());
        assertEquals(1, mutationCountBins.get(5).getCount().intValue());

        List<ClinicalDataBin> fractionGenomeAlteredBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("FRACTION_GENOME_ALTERED")).collect(Collectors.toList());
        assertEquals(7, fractionGenomeAlteredBins.size());
        assertEquals(1, fractionGenomeAlteredBins.get(0).getCount().intValue());
        assertEquals(1, fractionGenomeAlteredBins.get(1).getCount().intValue());
        assertEquals(1, fractionGenomeAlteredBins.get(2).getCount().intValue());
        assertEquals(1, fractionGenomeAlteredBins.get(3).getCount().intValue());
        assertEquals(1, fractionGenomeAlteredBins.get(4).getCount().intValue());
        assertEquals(1, fractionGenomeAlteredBins.get(5).getCount().intValue());
        assertEquals(1, fractionGenomeAlteredBins.get(6).getCount().intValue());

        List<ClinicalDataBin> ageAtSeqReportedYearsBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("AGE_AT_SEQ_REPORTED_YEARS")).collect(Collectors.toList());
        assertEquals(6, ageAtSeqReportedYearsBins.size());
        assertEquals(1, ageAtSeqReportedYearsBins.get(0).getCount().intValue());
        assertEquals(1, ageAtSeqReportedYearsBins.get(1).getCount().intValue());
        assertEquals(2, ageAtSeqReportedYearsBins.get(2).getCount().intValue());
        assertEquals(1, ageAtSeqReportedYearsBins.get(3).getCount().intValue());
        assertEquals(1, ageAtSeqReportedYearsBins.get(4).getCount().intValue());
        assertEquals(1, ageAtSeqReportedYearsBins.get(5).getCount().intValue());
        
        List<ClinicalDataBin> caAgeBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("CA_AGE")).collect(Collectors.toList());
        assertEquals(5, caAgeBins.size());
        assertEquals(1, caAgeBins.get(0).getCount().intValue());
        assertEquals(1, caAgeBins.get(1).getCount().intValue());
        assertEquals(1, caAgeBins.get(2).getCount().intValue());
        assertEquals(1, caAgeBins.get(3).getCount().intValue());
        assertEquals(1, caAgeBins.get(4).getCount().intValue());

        List<ClinicalDataBin> cptSeqBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("CPT_SEQ_DATE")).collect(Collectors.toList());
        assertEquals(3, cptSeqBins.size());
        assertEquals(1, cptSeqBins.get(0).getCount().intValue());
        assertEquals(3, cptSeqBins.get(1).getCount().intValue());
        assertEquals(3, cptSeqBins.get(2).getCount().intValue());
        
        List<ClinicalDataBin> cptOrderIntBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("CPT_ORDER_INT")).collect(Collectors.toList());
        assertEquals(1, cptOrderIntBins.size());
        assertEquals(7, cptOrderIntBins.get(0).getCount().intValue());
        
        List<ClinicalDataBin> hybridDeathIntBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("HYBRID_DEATH_INT")).collect(Collectors.toList());
        assertEquals(5, hybridDeathIntBins.size());
        assertEquals(1, hybridDeathIntBins.get(0).getCount().intValue());
        assertEquals(1, hybridDeathIntBins.get(1).getCount().intValue());
        assertEquals(1, hybridDeathIntBins.get(2).getCount().intValue());
        assertEquals(1, hybridDeathIntBins.get(3).getCount().intValue());
        assertEquals(1, hybridDeathIntBins.get(4).getCount().intValue());
        
        
        // assert function calls
        verify(idPopulator, times(1))
                    .populateIdLists(any(), any());
        
        // we don't expect filterClinicalData to be called for an unfiltered query
        verify(studyViewFilterUtil, never())
            .filterClinicalData(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        
        // study view filter should be applied only once
        verify(studyViewFilterApplier, times(1)).apply(any());
        
        // should call the correct bin calculate method only once for the given binning method
        verify(clinicalDataBinUtil, times(1))
            .calculateStaticDataBins(any(), any(), any(), any(), any(), any(), any(), any());
        verify(clinicalDataBinUtil, never())
            .calculateDynamicDataBins(any(), any(), any(), any(), any());
    }

    @Test
    public void testFilteredFetchClinicalDataBinCounts() {
        mockUnfilteredQuery();
        mockFilteredQuery();

        List<ClinicalDataBin> dataBins = clinicalDataBinUtil.fetchClinicalDataBinCounts(
            DataBinMethod.STATIC,
            mockQueryFilter()
        );

        // assert data bin counts
        
        assertEquals(33, dataBins.size());

        List<ClinicalDataBin> mutationCountBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("MUTATION_COUNT")).collect(Collectors.toList());
        assertEquals(6, mutationCountBins.size());
        assertEquals(0, mutationCountBins.get(0).getCount().intValue());
        assertEquals(0, mutationCountBins.get(1).getCount().intValue());
        assertEquals(0, mutationCountBins.get(2).getCount().intValue());
        assertEquals(1, mutationCountBins.get(3).getCount().intValue());
        assertEquals(1, mutationCountBins.get(4).getCount().intValue());
        assertEquals(0, mutationCountBins.get(5).getCount().intValue());

        List<ClinicalDataBin> fractionGenomeAlteredBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("FRACTION_GENOME_ALTERED")).collect(Collectors.toList());
        assertEquals(7, fractionGenomeAlteredBins.size());
        assertEquals(1, fractionGenomeAlteredBins.get(0).getCount().intValue());
        assertEquals(0, fractionGenomeAlteredBins.get(1).getCount().intValue());
        assertEquals(1, fractionGenomeAlteredBins.get(2).getCount().intValue());
        assertEquals(0, fractionGenomeAlteredBins.get(3).getCount().intValue());
        assertEquals(0, fractionGenomeAlteredBins.get(4).getCount().intValue());
        assertEquals(0, fractionGenomeAlteredBins.get(5).getCount().intValue());
        assertEquals(0, fractionGenomeAlteredBins.get(6).getCount().intValue());

        List<ClinicalDataBin> ageAtSeqReportedYearsBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("AGE_AT_SEQ_REPORTED_YEARS")).collect(Collectors.toList());
        assertEquals(6, ageAtSeqReportedYearsBins.size());
        assertEquals(1, ageAtSeqReportedYearsBins.get(0).getCount().intValue());
        assertEquals(1, ageAtSeqReportedYearsBins.get(1).getCount().intValue());
        assertEquals(0, ageAtSeqReportedYearsBins.get(2).getCount().intValue());
        assertEquals(0, ageAtSeqReportedYearsBins.get(3).getCount().intValue());
        assertEquals(0, ageAtSeqReportedYearsBins.get(4).getCount().intValue());
        assertEquals(0, ageAtSeqReportedYearsBins.get(5).getCount().intValue());

        List<ClinicalDataBin> caAgeBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("CA_AGE")).collect(Collectors.toList());
        assertEquals(5, caAgeBins.size());
        assertEquals(1, caAgeBins.get(0).getCount().intValue());
        assertEquals(1, caAgeBins.get(1).getCount().intValue());
        assertEquals(0, caAgeBins.get(2).getCount().intValue());
        assertEquals(0, caAgeBins.get(3).getCount().intValue());
        assertEquals(0, caAgeBins.get(4).getCount().intValue());

        List<ClinicalDataBin> cptSeqBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("CPT_SEQ_DATE")).collect(Collectors.toList());
        assertEquals(3, cptSeqBins.size());
        assertEquals(0, cptSeqBins.get(0).getCount().intValue());
        assertEquals(2, cptSeqBins.get(1).getCount().intValue());
        assertEquals(0, cptSeqBins.get(2).getCount().intValue());

        List<ClinicalDataBin> cptOrderIntBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("CPT_ORDER_INT")).collect(Collectors.toList());
        assertEquals(1, cptOrderIntBins.size());
        assertEquals(2, cptOrderIntBins.get(0).getCount().intValue());

        List<ClinicalDataBin> hybridDeathIntBins =
            dataBins.stream().filter(bin -> bin.getAttributeId().equals("HYBRID_DEATH_INT")).collect(Collectors.toList());
        assertEquals(5, hybridDeathIntBins.size());
        assertEquals(1, hybridDeathIntBins.get(0).getCount().intValue());
        assertEquals(0, hybridDeathIntBins.get(1).getCount().intValue());
        assertEquals(0, hybridDeathIntBins.get(2).getCount().intValue());
        assertEquals(1, hybridDeathIntBins.get(3).getCount().intValue());
        assertEquals(0, hybridDeathIntBins.get(4).getCount().intValue());
        
        
        // assert function calls
        
        // expect filterClinicalData to be called for a filtered query
        verify(studyViewFilterUtil, times(1))
            .filterClinicalData(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());

        // study view filter should be applied twice
        verify(studyViewFilterApplier, times(2)).apply(any());
        
        // should call the correct bin calculate method only once for the given binning method
        verify(clinicalDataBinUtil, times(1))
            .calculateStaticDataBins(any(), any(), any(), any(), any(), any(), any(), any());
        verify(clinicalDataBinUtil, never())
            .calculateDynamicDataBins(any(), any(), any(), any(), any());
    }    
    
    @Test
    public void fetchCustomDataBinCountsWithStaticBinningMethod() throws Exception {
        String customDataset = getFileContents("classpath:custom-dataset.json");
        
        mockCustomDataService(customDataset);
        mockStudyViewFilterApplier(customDataset);
        ClinicalDataBinCountFilter filter = createClinicalDataBinCountFilter();
        
        List<ClinicalDataBin> bins = clinicalDataBinUtil.fetchCustomDataBinCounts(
            DataBinMethod.STATIC,
            filter,
            false
        );
        
        // Total number of bins:
        assertEquals(11, bins.size());

        // All bins should have the custom data set name as attribute:
        List<ClinicalDataBin> customDatasetAttributeBins = bins
            .stream()
            .filter(bin -> bin.getAttributeId().equals(testDataAttributeId))
            .collect(Collectors.toList());
        assertEquals(11, customDatasetAttributeBins.size());

        assertEquals("<=", bins.get(0).getSpecialValue());
        assertEquals(3, bins.get(0).getCount().intValue());

        // Bin size should be five:
        assertEquals(5, bins.get(1).getEnd().intValue() - bins.get(1).getStart().intValue());
        assertEquals(5, bins.get(1).getCount().intValue());

        assertEquals(8, bins.get(2).getCount().intValue());
        assertEquals(5, bins.get(3).getCount().intValue());
        assertEquals(8, bins.get(4).getCount().intValue());
        assertEquals(5, bins.get(5).getCount().intValue());
        assertEquals(8, bins.get(6).getCount().intValue());
        assertEquals(5, bins.get(7).getCount().intValue());
        assertEquals(6, bins.get(8).getCount().intValue());
        assertEquals(2, bins.get(9).getCount().intValue());
        
        assertEquals(">", bins.get(10).getSpecialValue());
        assertEquals(1, bins.get(10).getCount().intValue());
    }

    @Test
    public void fetchCustomDataBinCountsWithStaticBinningMethod_minimalExample() throws Exception {
        String customDataset = getFileContents("classpath:custom-dataset-minimal.json");
        
        mockCustomDataService(customDataset);
        mockStudyViewFilterApplier(customDataset);
        ClinicalDataBinCountFilter filter = createClinicalDataBinCountFilter();
        
        List<ClinicalDataBin> bins = clinicalDataBinUtil.fetchCustomDataBinCounts(
            DataBinMethod.STATIC,
            filter,
            false
        );
        
        // Total number of bins:
        assertEquals(13, bins.size());

        // All bins should have the custom data set name as attribute:
        List<ClinicalDataBin> customDatasetAttributeBins = bins
            .stream()
            .filter(bin -> bin.getAttributeId().equals(testDataAttributeId))
            .collect(Collectors.toList());
        assertEquals(13, customDatasetAttributeBins.size());
        
        // Start bin:
        assertEquals("<=", bins.get(0).getSpecialValue());
        assertEquals(1, bins.get(0).getCount().intValue());
        assertEquals(-1.0, bins.get(0).getEnd().intValue(), 0);

        // Size of next bins should be 1:
        assertEquals(1, bins.get(1).getEnd().intValue() - bins.get(1).getStart().intValue(), 0);
        assertEquals(1, bins.get(1).getCount().intValue());

        assertEquals(1, bins.get(2).getCount().intValue());
        assertEquals(2, bins.get(3).getCount().intValue());
        assertEquals(2, bins.get(4).getCount().intValue());
        assertEquals(1, bins.get(5).getCount().intValue());
        assertEquals(0, bins.get(6).getCount().intValue());
        assertEquals(1, bins.get(7).getCount().intValue());
        assertEquals(1, bins.get(8).getCount().intValue());
        assertEquals(1, bins.get(9).getCount().intValue());
        assertEquals(1, bins.get(10).getCount().intValue());
        assertEquals(1, bins.get(11).getCount().intValue());
        assertEquals(1, bins.get(12).getCount().intValue());
        
    }

    private void mockStudyViewFilterApplier(String customDataset) throws IOException {
        TreeNode path = customDatasetMapper.readTree(customDataset).path("data").path("data");

        TypeReference<List<SampleIdentifier>> type = new TypeReference<List<SampleIdentifier>>() {};
        List<SampleIdentifier> customIDs = customDatasetMapper.readValue(customDatasetMapper.treeAsTokens(path), type);

        when(
            studyViewFilterApplier.apply(any())
        ).thenReturn(customIDs);
    }

    private String getFileContents(String resourceLocation) throws IOException {
        return new String(Files.readAllBytes(ResourceUtils.getFile(resourceLocation).toPath()));
    }

    private void mockUnfilteredQuery()
    {
        mockMethods(
            mockUnfilteredSampleIdentifiers(),
            mockUnfilteredSampleIds(),
            mockUnfilteredStudyIds(),
            mockUnfilteredSampleAttributeIds(),
            mockUnfilteredStudySampleUniqueKeys(),
            mockUnfilteredClinicalDataForSamples(),
            mockUnfilteredPatients(),
            mockUnfilteredPatientIds(),
            mockUnfilteredStudyIdsOfPatients(),
            mockUnfilteredPatientAttributeIds(),
            mockUnfilteredStudyPatientUniqueKeys(),
            mockUnfilteredClinicalDataForPatients(),
            mockUnfilteredStudyViewFilter(),
            mockUnfilteredAttributeIds(),
            mockUnfilteredClinicalAttributes()
        );
    }

    private void mockFilteredQuery()
    {
        mockMethods(
            mockFilteredSampleIdentifiers(),
            mockFilteredSampleIds(),
            mockFilteredStudyIds(),
            mockUnfilteredSampleAttributeIds(),
            mockFilteredStudySampleUniqueKeys(),
            null,
            mockFilteredPatients(),
            mockFilteredPatientIds(),
            mockFilteredStudyIdsOfPatients(),
            mockUnfilteredPatientAttributeIds(),
            mockFilteredStudyPatientUniqueKeys(),
            null,
            mockFilteredStudyViewFilter(),
            mockUnfilteredAttributeIds(),
            mockFilteredClinicalAttributes()
        );
    }

    private static final String sessionTestKey = "testkey";

    @Value("classpath:state.json") Resource stateFile;

    private void mockCustomDataService(String customDataset) throws Exception {
        when(
            sessionServiceRequestHandler.getSessionDataJson(any(), any())
        ).thenReturn(customDataset);
    }

    private void mockMethods(
        List<SampleIdentifier> sampleIdentifiers,
        List<String> sampleIds,
        List<String> studyIds,
        List<String> sampleAttributeIds,
        List<String> studySampleUniqueKeys,
        List<ClinicalData> clinicalDataForSamples,
        List<Patient> patients,
        List<String> patientIds,
        List<String> studyIdsOfPatients,
        List<String> patientAttributeIds,
        List<String> studyPatientUniqueKeys,
        List<ClinicalData> clinicalDataForPatients,
        StudyViewFilter studyViewFilter,
        List<String> attributeIds,
        List<ClinicalAttribute> clinicalAttributes
    ) {
        when(
            patientService.getPatientsOfSamples(eq(studyIds), eq(sampleIds))
        ).thenReturn(patients);
        when(
            clinicalAttributeService.getClinicalAttributesByStudyIdsAndAttributeIds(eq(studyIds), eq(attributeIds))
        ).thenReturn(clinicalAttributes);
        when(
            studyViewFilterApplier.apply(argThat(new StudyViewFilterMatcher(studyViewFilter)))
        ).thenReturn(sampleIdentifiers);
        when(
            studyViewFilterApplier.getUniqkeyKeys(eq(studyIds), eq(sampleIds))
        ).thenReturn(studySampleUniqueKeys);
        when(
            studyViewFilterApplier.getUniqkeyKeys(eq(studyIdsOfPatients), eq(patientIds))
        ).thenReturn(studyPatientUniqueKeys);

        when(
            clinicalDataFetcher.fetchClinicalDataForSamples(eq(studyIds), eq(sampleIds), eq(sampleAttributeIds))
        ).thenReturn(clinicalDataForSamples);
        when(
            clinicalDataFetcher.fetchClinicalDataForPatients(eq(studyIdsOfPatients), eq(patientIds), eq(patientAttributeIds))
        ).thenReturn(clinicalDataForPatients);
    }

    private ClinicalDataBinCountFilter mockBaseFilter() {
        ClinicalDataBinCountFilter filter = new ClinicalDataBinCountFilter();
        
        filter.setAttributes(mockUnfilteredAttributes());
        filter.setStudyViewFilter(mockUnfilteredStudyViewFilter());
        
        return filter;
    }

    private ClinicalDataBinCountFilter mockQueryFilter() {
        ClinicalDataBinCountFilter filter = new ClinicalDataBinCountFilter();

        filter.setAttributes(mockUnfilteredAttributes());
        filter.setStudyViewFilter(mockFilteredStudyViewFilter());

        return filter;
    }

    private List<String> mockUnfilteredStudySampleUniqueKeys() {
        List<String> keys = new ArrayList<>();
        
        keys.add("genie_bpc_testGENIE-MSK-P-0003156-T01-IM5");
        keys.add("genie_bpc_testGENIE-MSK-P-0009680-T01-IM5");
        keys.add("genie_bpc_testGENIE-MSK-P-0012393-T01-IM5");
        keys.add("genie_bpc_testGENIE-MSK-P-0012393-T02-IM6");
        keys.add("genie_bpc_testGENIE-MSK-P-0015492-T01-IM6");
        keys.add("genie_bpc_testGENIE-MSK-P-0017284-T01-IM6");
        keys.add("genie_bpc_testGENIE-MSK-P-0017284-T02-IM5");
        
        return keys;
    }

    private List<String> mockFilteredStudySampleUniqueKeys() {
        List<String> keys = new ArrayList<>();
        
        keys.add("genie_bpc_testGENIE-MSK-P-0009680-T01-IM5");
        keys.add("genie_bpc_testGENIE-MSK-P-0015492-T01-IM6");
        
        return keys;
    }

    private List<String> mockUnfilteredStudyPatientUniqueKeys() {
        List<String> keys = new ArrayList<>();
        
        keys.add("genie_bpc_testGENIE-MSK-P-0015492");
        keys.add("genie_bpc_testGENIE-MSK-P-0003156");
        keys.add("genie_bpc_testGENIE-MSK-P-0009680");
        keys.add("genie_bpc_testGENIE-MSK-P-0012393");
        keys.add("genie_bpc_testGENIE-MSK-P-0017284");
        
        return keys;
    }

    private List<String> mockFilteredStudyPatientUniqueKeys() {
        List<String> keys = new ArrayList<>();
        
        keys.add("genie_bpc_testGENIE-MSK-P-0015492");
        keys.add("genie_bpc_testGENIE-MSK-P-0009680");
        
        return keys;
    }

    private List<String> mockUnfilteredSampleIds() {
        List<String> sampleIds = new ArrayList<>();

        sampleIds.add("GENIE-MSK-P-0003156-T01-IM5");
        sampleIds.add("GENIE-MSK-P-0009680-T01-IM5");
        sampleIds.add("GENIE-MSK-P-0012393-T01-IM5");
        sampleIds.add("GENIE-MSK-P-0012393-T02-IM6");
        sampleIds.add("GENIE-MSK-P-0015492-T01-IM6");
        sampleIds.add("GENIE-MSK-P-0017284-T01-IM6");
        sampleIds.add("GENIE-MSK-P-0017284-T02-IM5");

        return sampleIds;
    }

    private List<String> mockFilteredSampleIds() {
        List<String> sampleIds = new ArrayList<>();

        sampleIds.add("GENIE-MSK-P-0009680-T01-IM5");
        sampleIds.add("GENIE-MSK-P-0015492-T01-IM6");

        return sampleIds;
    }

    private List<String> mockUnfilteredPatientIds() {
        List<String> patientIds = new ArrayList<>();

        patientIds.add("GENIE-MSK-P-0015492");
        patientIds.add("GENIE-MSK-P-0003156");
        patientIds.add("GENIE-MSK-P-0009680");
        patientIds.add("GENIE-MSK-P-0012393");
        patientIds.add("GENIE-MSK-P-0017284");

        return patientIds;
    }

    private List<String> mockFilteredPatientIds() {
        List<String> patientIds = new ArrayList<>();

        patientIds.add("GENIE-MSK-P-0015492");
        patientIds.add("GENIE-MSK-P-0009680");

        return patientIds;
    }

    private List<Patient> mockUnfilteredPatients() {
        return mockPatients(mockUnfilteredPatientIds());
    }

    private List<Patient> mockFilteredPatients() {
        return mockPatients(mockFilteredPatientIds());
    }

    private List<Patient> mockPatients(List<String> patientIds) {
        List<Patient> patients = new ArrayList<>();
        
        for (String patientId: patientIds) {
            Patient patient = new Patient();
            patient.setCancerStudyIdentifier(STUDY_ID);
            patient.setStableId(patientId);
            patients.add(patient);
        }
        
        return patients;
    }

    private List<String> mockUnfilteredStudyIds() {
        return Collections.nCopies(7, STUDY_ID);
    }

    private List<String> mockFilteredStudyIds() {
        return Collections.nCopies(2, STUDY_ID);
    }

    private List<String> mockUnfilteredStudyIdsOfPatients() {
        return Collections.nCopies(5, STUDY_ID);
    }

    private List<String> mockFilteredStudyIdsOfPatients() {
        return Collections.nCopies(2, STUDY_ID);
    }

    private List<SampleIdentifier> mockUnfilteredSampleIdentifiers() {
        return mockSampleIdentifiers(mockUnfilteredSampleIds());
    }

    private List<SampleIdentifier> mockFilteredSampleIdentifiers() {
        return mockSampleIdentifiers(mockFilteredSampleIds());
    }

    private List<SampleIdentifier> mockSampleIdentifiers(List<String> sampleIds) {
        List<SampleIdentifier> sampleIdentifiers = new ArrayList<>();

        for (String sampleId: sampleIds) {
            SampleIdentifier identifier = new SampleIdentifier();
            identifier.setSampleId(sampleId);
            identifier.setStudyId(STUDY_ID);
            sampleIdentifiers.add(identifier);
        }

        return sampleIdentifiers;
    }

    private List<String> mockUnfilteredAttributeIds() {
        List<String> attributeIds = new ArrayList<>();
        
        attributeIds.addAll(mockUnfilteredSampleAttributeIds());
        attributeIds.addAll(mockUnfilteredPatientAttributeIds());
        
        return attributeIds;
    }

    private List<String> mockUnfilteredSampleAttributeIds() {
        List<String> attributeIds = new ArrayList<>();
        
        attributeIds.add("CPT_SEQ_DATE");
        attributeIds.add("CPT_ORDER_INT");
        attributeIds.add("MUTATION_COUNT");
        attributeIds.add("AGE_AT_SEQ_REPORTED_YEARS");
        attributeIds.add("FRACTION_GENOME_ALTERED");

        return attributeIds;
    }

    private List<String> mockUnfilteredPatientAttributeIds() {
        List<String> attributeIds = new ArrayList<>();

        attributeIds.add("CA_AGE");
        attributeIds.add("HYBRID_DEATH_INT");

        return attributeIds;
    }

    private List<ClinicalDataBinFilter> mockUnfilteredAttributes() {
        List<ClinicalDataBinFilter> attributes = new ArrayList<>();
        List<String> attributeIds = mockUnfilteredAttributeIds();
        
        for (String attributeId: attributeIds) {
            ClinicalDataBinFilter filter = new ClinicalDataBinFilter();
            filter.setAttributeId(attributeId);
            attributes.add(filter);
        }
        
        return attributes;
    }

    private ClinicalAttribute mockClinicalAttribute(String attrId, String displayName, boolean isPatientAttribute) {
        ClinicalAttribute attr = new ClinicalAttribute();
        
        attr.setAttrId(attrId);
        attr.setDisplayName(displayName);
        attr.setDatatype("NUMBER");
        attr.setPatientAttribute(isPatientAttribute);
        attr.setCancerStudyId(STUDY_ID_INT);
        attr.setCancerStudyIdentifier(STUDY_ID);
        
        return attr;
    }

    private List<ClinicalAttribute> mockUnfilteredClinicalAttributes() {
        List<ClinicalAttribute> clinicalAttributes = new ArrayList<>();
        
        clinicalAttributes.add(mockClinicalAttribute("AGE_AT_SEQ_REPORTED_YEARS", "Age at Which Sequencing was Reported (Years)", false));
        clinicalAttributes.add(mockClinicalAttribute("CA_AGE", "Curated Patient Age at Diagnosis", true));
        clinicalAttributes.add(mockClinicalAttribute("CPT_ORDER_INT", "Interval in Days from Date of Birth to Cancer Order Date", false));
        clinicalAttributes.add(mockClinicalAttribute("CPT_SEQ_DATE", "GENIE Sequence Date (quarter year)", false));
        clinicalAttributes.add(mockClinicalAttribute("FRACTION_GENOME_ALTERED", "Fraction Genome Altered", false));
        clinicalAttributes.add(mockClinicalAttribute("HYBRID_DEATH_INT", "Interval in Months from DOB to Date of Death", true));
        clinicalAttributes.add(mockClinicalAttribute("MUTATION_COUNT", "Mutation Count", false));

        return clinicalAttributes;
    }

    private List<ClinicalAttribute> mockFilteredClinicalAttributes() {
        List<ClinicalAttribute> clinicalAttributes = new ArrayList<>();

        clinicalAttributes.add(mockClinicalAttribute("AGE_AT_SEQ_REPORTED_YEARS", "Age at Which Sequencing was Reported (Years)", false));
        clinicalAttributes.add(mockClinicalAttribute("CA_AGE", "Curated Patient Age at Diagnosis", true));
        clinicalAttributes.add(mockClinicalAttribute("CPT_SEQ_DATE", "GENIE Sequence Date (quarter year)", false));
        clinicalAttributes.add(mockClinicalAttribute("FRACTION_GENOME_ALTERED", "Fraction Genome Altered", false));
        clinicalAttributes.add(mockClinicalAttribute("HYBRID_DEATH_INT", "Interval in Months from DOB to Date of Death", true));
        clinicalAttributes.add(mockClinicalAttribute("MUTATION_COUNT", "Mutation Count", false));

        return clinicalAttributes;
    }

    private ClinicalData mockClinicalData(String id, String value, String patientId, String sampleId)
    {
        ClinicalData data = new ClinicalData();
        
        data.setAttrId(id);
        data.setAttrValue(value);
        data.setPatientId(patientId);
        data.setSampleId(sampleId);
        data.setStudyId(STUDY_ID);
        
        return data;
    }

    private List<ClinicalData> mockUnfilteredClinicalDataForSamples() {
        List<ClinicalData> data = new ArrayList<>();
        
        data.add(mockClinicalData("AGE_AT_SEQ_REPORTED_YEARS", "53.03", "GENIE-MSK-P-0015492", "GENIE-MSK-P-0015492-T01-IM6"));
        data.add(mockClinicalData("CPT_SEQ_DATE", "2016", "GENIE-MSK-P-0015492", "GENIE-MSK-P-0015492-T01-IM6"));
        data.add(mockClinicalData("FRACTION_GENOME_ALTERED", "0.0460", "GENIE-MSK-P-0015492", "GENIE-MSK-P-0015492-T01-IM6"));
        data.add(mockClinicalData("MUTATION_COUNT", "7", "GENIE-MSK-P-0015492", "GENIE-MSK-P-0015492-T01-IM6"));
        data.add(mockClinicalData("AGE_AT_SEQ_REPORTED_YEARS", "78.05", "GENIE-MSK-P-0003156", "GENIE-MSK-P-0003156-T01-IM5"));
        data.add(mockClinicalData("CPT_SEQ_DATE", "2015", "GENIE-MSK-P-0003156", "GENIE-MSK-P-0003156-T01-IM5"));
        data.add(mockClinicalData("FRACTION_GENOME_ALTERED", "0.2164", "GENIE-MSK-P-0003156", "GENIE-MSK-P-0003156-T01-IM5"));
        data.add(mockClinicalData("MUTATION_COUNT", "8", "GENIE-MSK-P-0003156", "GENIE-MSK-P-0003156-T01-IM5"));
        data.add(mockClinicalData("AGE_AT_SEQ_REPORTED_YEARS", "59.04", "GENIE-MSK-P-0009680", "GENIE-MSK-P-0009680-T01-IM5"));
        data.add(mockClinicalData("CPT_SEQ_DATE", "2016", "GENIE-MSK-P-0009680", "GENIE-MSK-P-0009680-T01-IM5"));
        data.add(mockClinicalData("FRACTION_GENOME_ALTERED", "0.2073", "GENIE-MSK-P-0009680", "GENIE-MSK-P-0009680-T01-IM5"));
        data.add(mockClinicalData("MUTATION_COUNT", "6", "GENIE-MSK-P-0009680", "GENIE-MSK-P-0009680-T01-IM5"));
        data.add(mockClinicalData("AGE_AT_SEQ_REPORTED_YEARS", "72.04", "GENIE-MSK-P-0012393", "GENIE-MSK-P-0012393-T01-IM5"));
        data.add(mockClinicalData("CPT_SEQ_DATE", "2016", "GENIE-MSK-P-0012393", "GENIE-MSK-P-0012393-T01-IM5"));
        data.add(mockClinicalData("FRACTION_GENOME_ALTERED", "0.5280", "GENIE-MSK-P-0012393", "GENIE-MSK-P-0012393-T01-IM5"));
        data.add(mockClinicalData("MUTATION_COUNT", "3", "GENIE-MSK-P-0012393", "GENIE-MSK-P-0012393-T01-IM5"));
        data.add(mockClinicalData("AGE_AT_SEQ_REPORTED_YEARS", "73.04", "GENIE-MSK-P-0012393", "GENIE-MSK-P-0012393-T02-IM6"));
        data.add(mockClinicalData("CPT_SEQ_DATE", "2017", "GENIE-MSK-P-0012393", "GENIE-MSK-P-0012393-T02-IM6"));
        data.add(mockClinicalData("FRACTION_GENOME_ALTERED", "0.1297", "GENIE-MSK-P-0012393", "GENIE-MSK-P-0012393-T02-IM6"));
        data.add(mockClinicalData("MUTATION_COUNT", "3", "GENIE-MSK-P-0012393", "GENIE-MSK-P-0012393-T02-IM6"));
        data.add(mockClinicalData("AGE_AT_SEQ_REPORTED_YEARS", "64.04", "GENIE-MSK-P-0017284", "GENIE-MSK-P-0017284-T01-IM6"));
        data.add(mockClinicalData("CPT_SEQ_DATE", "2017", "GENIE-MSK-P-0017284", "GENIE-MSK-P-0017284-T01-IM6"));
        data.add(mockClinicalData("FRACTION_GENOME_ALTERED", "0.2796", "GENIE-MSK-P-0017284", "GENIE-MSK-P-0017284-T01-IM6"));
        data.add(mockClinicalData("MUTATION_COUNT", "5", "GENIE-MSK-P-0017284", "GENIE-MSK-P-0017284-T01-IM6"));
        data.add(mockClinicalData("AGE_AT_SEQ_REPORTED_YEARS", "64.04", "GENIE-MSK-P-0017284", "GENIE-MSK-P-0017284-T02-IM5"));
        data.add(mockClinicalData("CPT_SEQ_DATE", "2017", "GENIE-MSK-P-0017284", "GENIE-MSK-P-0017284-T02-IM5"));
        data.add(mockClinicalData("FRACTION_GENOME_ALTERED", "0.7233", "GENIE-MSK-P-0017284", "GENIE-MSK-P-0017284-T02-IM5"));
        data.add(mockClinicalData("MUTATION_COUNT", "4", "GENIE-MSK-P-0017284", "GENIE-MSK-P-0017284-T02-IM5"));
        
        return data;
    }

    private List<ClinicalData> mockUnfilteredClinicalDataForPatients() {
        List<ClinicalData> data = new ArrayList<>();

        data.add(mockClinicalData("CA_AGE", "52", "GENIE-MSK-P-0015492", null));
        data.add(mockClinicalData("HYBRID_DEATH_INT", "657.3", "GENIE-MSK-P-0015492", null));
        data.add(mockClinicalData("CA_AGE", "77", "GENIE-MSK-P-0003156", null));
        data.add(mockClinicalData("HYBRID_DEATH_INT", "968.1", "GENIE-MSK-P-0003156", null));
        data.add(mockClinicalData("CA_AGE", "56", "GENIE-MSK-P-0009680", null));
        data.add(mockClinicalData("HYBRID_DEATH_INT", "8765.7", "GENIE-MSK-P-0009680", null));
        data.add(mockClinicalData("CA_AGE", "71", "GENIE-MSK-P-0012393", null));
        data.add(mockClinicalData("HYBRID_DEATH_INT", "730.4", "GENIE-MSK-P-0012393", null));
        data.add(mockClinicalData("CA_AGE", "62", "GENIE-MSK-P-0017284", null));

        return data;
    }

    private StudyViewFilter mockUnfilteredStudyViewFilter() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();

        List<String> studyIds = new ArrayList<>();
        studyIds.add("genie_bpc_test");
        studyViewFilter.setStudyIds(studyIds);
        
        return studyViewFilter;
    }

    private StudyViewFilter mockFilteredStudyViewFilter() {
        StudyViewFilter studyViewFilter = mockUnfilteredStudyViewFilter();
        
        ClinicalDataFilter sexFilter = new ClinicalDataFilter();
        sexFilter.setAttributeId("SEX");
        List<DataFilterValue> sexValues = new ArrayList<>();
        DataFilterValue female = new DataFilterValue();
        female.setValue("Female");
        sexValues.add(female);
        sexFilter.setValues(sexValues);

        ClinicalDataFilter sampleCountFilter = new ClinicalDataFilter();
        sampleCountFilter.setAttributeId("SAMPLE_COUNT");
        List<DataFilterValue> sampleCountValues = new ArrayList<>();
        DataFilterValue one = new DataFilterValue();
        one.setValue("1");
        sampleCountValues.add(one);
        sampleCountFilter.setValues(sampleCountValues);

        List<ClinicalDataFilter> filters = new ArrayList<>();
        filters.add(sexFilter);
        filters.add(sampleCountFilter);
        
        studyViewFilter.setClinicalDataFilters(filters);
        
        return studyViewFilter;
    }

    private class StudyViewFilterMatcher implements ArgumentMatcher<StudyViewFilter> {

        private StudyViewFilter source;
        public StudyViewFilterMatcher(StudyViewFilter source) {
            this.source = source;
        }

        @Override
        public boolean matches(StudyViewFilter target) {
            return (
                target != null &&
                Objects.equals(target.getStudyIds(), source.getStudyIds()) &&
                Objects.equals(target.getSampleIdentifiers(), source.getSampleIdentifiers()) && 
                equalClinicalDataFilters(source.getClinicalDataFilters(), target.getClinicalDataFilters())
            );
        }

        private boolean equalClinicalDataFilters(List<ClinicalDataFilter> sourceFilters, List<ClinicalDataFilter> targetFilters) {
            if (sourceFilters == null && targetFilters == null) {
                return true;
            }
            
            if (sourceFilters != null && targetFilters != null && sourceFilters.size() == targetFilters.size()) {
                for (int i = 0; i < sourceFilters.size(); i++) {
                    if (
                        !Objects.equals(sourceFilters.get(i).getAttributeId(), targetFilters.get(i).getAttributeId()) ||
                        !equalDataFilterValues(sourceFilters.get(i).getValues(), targetFilters.get(i).getValues())
                    ) {
                        return false;
                    }
                }
            }
            else {
                return false;
            }
            
            return true;
        }

        private boolean equalDataFilterValues(List<DataFilterValue> sourceValues, List<DataFilterValue> targetValues)
        {
            if (sourceValues == null && targetValues == null) {
                return true;
            }

            if (sourceValues != null && targetValues != null && sourceValues.size() == targetValues.size()) {
                for (int i = 0; i < sourceValues.size(); i++) {
                    if (
                        !Objects.equals(sourceValues.get(i).getStart(), targetValues.get(i).getStart()) ||
                        !Objects.equals(sourceValues.get(i).getEnd(), targetValues.get(i).getEnd()) ||
                        !Objects.equals(sourceValues.get(i).getValue(), targetValues.get(i).getValue())
                    ) {
                        return false;
                    }
                }
            }
            else {
                return false;
            }

            return true;
        }

    }

    private ClinicalDataBinCountFilter createClinicalDataBinCountFilter() {
        ClinicalDataBinCountFilter clinicalDataBinCountFilter = new ClinicalDataBinCountFilter();
        List<ClinicalDataBinFilter> attributes = new ArrayList<>();
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(testDataAttributeId);
        clinicalDataBinFilter.setBinMethod(DataBinFilter.BinMethod.CUSTOM);
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        ArrayList<String> studyIds = new ArrayList<>();
        studyIds.add("study_es_0");
        studyViewFilter.setStudyIds(studyIds);
        clinicalDataBinCountFilter.setStudyViewFilter(studyViewFilter);
        attributes.add(clinicalDataBinFilter);
        clinicalDataBinCountFilter.setAttributes(attributes);
        return clinicalDataBinCountFilter;
    }
}
