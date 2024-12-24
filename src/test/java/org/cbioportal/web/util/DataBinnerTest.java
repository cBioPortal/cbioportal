package org.cbioportal.web.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.cbioportal.web.parameter.DataBinFilter.*;


import java.util.stream.IntStream;

import org.cbioportal.model.Binnable;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DataBin;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.cbioportal.web.parameter.BinsGeneratorConfig;
import org.cbioportal.web.parameter.ClinicalDataBinFilter;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {
    DataBinner.class,
    DataBinHelper.class,
    DiscreteDataBinner.class,
    LogScaleDataBinner.class,
    LinearDataBinner.class,
    ScientificSmallDataBinner.class,
    StudyViewFilterUtil.class
 })
public class DataBinnerTest {

    private Map<String, String[]> mockData;

    @Autowired
    private DataBinner dataBinner;

    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;

    @MockBean
    private MolecularProfileUtil molecularProfileUtil;
    
    @MockBean
    private GeneService geneService;

    private List<String> getCaseIds(List<Binnable> unfilteredClinicalData, boolean getPatientIds) {
        return unfilteredClinicalData
                .stream()
                .map(datum -> studyViewFilterUtil.getCaseUniqueKey(datum.getStudyId(),
                getPatientIds ? datum.getPatientId() : datum.getSampleId()))
                .collect(Collectors.toList());
    }

    @Before
    public void setup() {
        mockData = DataBinnerMocker.mockData();
    }

    @Test
    public void testLinearDataBinner() {
        String studyId = "blca_tcga";
        String attributeId = "AGE";
        String[] values = mockData.get("blca_tcga_AGE");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        Assert.assertEquals(11, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("40.0"), dataBins.get(0).getEnd());
        Assert.assertEquals(2, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("40.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("45.0"), dataBins.get(1).getEnd());
        Assert.assertEquals(6, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new BigDecimal("45.0"), dataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("50.0"), dataBins.get(2).getEnd());
        Assert.assertEquals(17, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new BigDecimal("50.0"), dataBins.get(3).getStart());
        Assert.assertEquals(new BigDecimal("55.0"), dataBins.get(3).getEnd());
        Assert.assertEquals(24, dataBins.get(3).getCount().intValue());

        Assert.assertEquals(new BigDecimal("55.0"), dataBins.get(4).getStart());
        Assert.assertEquals(new BigDecimal("60.0"), dataBins.get(4).getEnd());
        Assert.assertEquals(59, dataBins.get(4).getCount().intValue());

        Assert.assertEquals(new BigDecimal("60.0"), dataBins.get(5).getStart());
        Assert.assertEquals(new BigDecimal("65.0"), dataBins.get(5).getEnd());
        Assert.assertEquals(53, dataBins.get(5).getCount().intValue());

        Assert.assertEquals(new BigDecimal("65.0"), dataBins.get(6).getStart());
        Assert.assertEquals(new BigDecimal("70.0"), dataBins.get(6).getEnd());
        Assert.assertEquals(71, dataBins.get(6).getCount().intValue());

        Assert.assertEquals(new BigDecimal("70.0"), dataBins.get(7).getStart());
        Assert.assertEquals(new BigDecimal("75.0"), dataBins.get(7).getEnd());
        Assert.assertEquals(67, dataBins.get(7).getCount().intValue());

        Assert.assertEquals(new BigDecimal("75.0"), dataBins.get(8).getStart());
        Assert.assertEquals(new BigDecimal("80.0"), dataBins.get(8).getEnd());
        Assert.assertEquals(64, dataBins.get(8).getCount().intValue());

        Assert.assertEquals(new BigDecimal("80.0"), dataBins.get(9).getStart());
        Assert.assertEquals(new BigDecimal("85.0"), dataBins.get(9).getEnd());
        Assert.assertEquals(35, dataBins.get(9).getCount().intValue());

        Assert.assertEquals(new BigDecimal("85.0"), dataBins.get(10).getStart());
        Assert.assertEquals(new BigDecimal("90.0"), dataBins.get(10).getEnd());
        Assert.assertEquals(13, dataBins.get(10).getCount().intValue());
    }


    @Test
    public void testLinearDataBinnerWithRange() {
        String studyId = "random";
        String attributeId = "random";
        String[] values = mockData.get("linear_integer_continuous");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setStart(new BigDecimal("39.5"));
        clinicalDataBinFilter.setEnd(new BigDecimal("80.5"));

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        Assert.assertEquals(8, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("45.0"), dataBins.get(0).getEnd());
        Assert.assertEquals(6, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("45.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("50.0"), dataBins.get(1).getEnd());
        Assert.assertEquals(5, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new BigDecimal("50.0"), dataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("55.0"), dataBins.get(2).getEnd());
        Assert.assertEquals(5, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new BigDecimal("75.0"), dataBins.get(7).getStart());
        Assert.assertEquals(new BigDecimal("80.0"), dataBins.get(7).getEnd());
        Assert.assertEquals(5, dataBins.get(7).getCount().intValue());
    }


    @Test
    public void testLinearDataBinnerWithRangeOne() {
        String studyId = "random";
        String attributeId = "random";
        String[] values = mockData.get("linear_integer_continuous");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setStart(new BigDecimal("39.5"));
        clinicalDataBinFilter.setEnd(new BigDecimal("80.5"));

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        Assert.assertEquals(8, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("45.0"), dataBins.get(0).getEnd());
        Assert.assertEquals(6, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("45.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("50.0"), dataBins.get(1).getEnd());
        Assert.assertEquals(5, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new BigDecimal("50.0"), dataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("55.0"), dataBins.get(2).getEnd());
        Assert.assertEquals(5, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new BigDecimal("75.0"), dataBins.get(7).getStart());
        Assert.assertEquals(new BigDecimal("80.0"), dataBins.get(7).getEnd());
        Assert.assertEquals(5, dataBins.get(7).getCount().intValue());
    }

    @Test
    public void testLinearDataBinnerWithRangeTwo() {
        String studyId = "random";
        String attributeId = "random";
        String[] values = mockData.get("linear_integer_continuous");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setStart(new BigDecimal("39.5"));
        clinicalDataBinFilter.setEnd(new BigDecimal("81.5"));

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        Assert.assertEquals(9, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("45.0"), dataBins.get(0).getEnd());
        Assert.assertEquals(6, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("45.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("50.0"), dataBins.get(1).getEnd());
        Assert.assertEquals(5, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new BigDecimal("50.0"), dataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("55.0"), dataBins.get(2).getEnd());
        Assert.assertEquals(5, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new BigDecimal("75.0"), dataBins.get(7).getStart());
        Assert.assertEquals(new BigDecimal("80.0"), dataBins.get(7).getEnd());
        Assert.assertEquals(5, dataBins.get(7).getCount().intValue());

        Assert.assertEquals(new BigDecimal("80.0"), dataBins.get(8).getStart());
        Assert.assertEquals(">", dataBins.get(8).getSpecialValue());
        Assert.assertEquals(1, dataBins.get(8).getCount().intValue());
    }

    @Test
    public void testLinearDataBinnerWithRangeAndCustomBins() {
        String studyId = "random";
        String attributeId = "random";
        String[] values = mockData.get("linear_integer_continuous");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setStart(new BigDecimal("39.5"));
        clinicalDataBinFilter.setEnd(new BigDecimal("81.5"));
        clinicalDataBinFilter.setBinMethod(BinMethod.CUSTOM);
        clinicalDataBinFilter.setCustomBins(Arrays.asList(50.0, 60.0, 70.0).stream().map(item -> BigDecimal.valueOf(item)).collect(Collectors.toList()));

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        Assert.assertEquals(4, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("50.0"), dataBins.get(0).getEnd());
        Assert.assertEquals(11, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("50.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("60.0"), dataBins.get(1).getEnd());
        Assert.assertEquals(10, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new BigDecimal("60.0"), dataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("70.0"), dataBins.get(2).getEnd());
        Assert.assertEquals(10, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new BigDecimal("70.0"), dataBins.get(3).getStart());
        Assert.assertEquals(">", dataBins.get(3).getSpecialValue());
        Assert.assertEquals(11, dataBins.get(3).getCount().intValue());
    }

    @Test
    public void testStaticDataBinnerFilter() {
        String studyId = "blca_tcga";
        String attributeId = "AGE";
        String[] values = mockData.get("blca_tcga_AGE");

        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<Binnable> unfilteredClinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> unfilteredPatientIds =
            getCaseIds(unfilteredClinicalData, true);
        List<DataBin> unfilteredDataBins = dataBinner.calculateDataBins(
            clinicalDataBinFilter, ClinicalDataType.PATIENT, unfilteredClinicalData, unfilteredPatientIds);
        
        List<Binnable> filteredClinicalData = unfilteredClinicalData.subList(0, 108); // (0, 60] interval
        List<String> filteredPatientIds =
            getCaseIds(filteredClinicalData, true);
        List<DataBin> filteredDataBins = dataBinner.calculateClinicalDataBins(clinicalDataBinFilter,
                ClinicalDataType.PATIENT, filteredClinicalData, unfilteredClinicalData, filteredPatientIds,
                unfilteredPatientIds);

        // same number of bins for both
        Assert.assertEquals(11, unfilteredDataBins.size());
        Assert.assertEquals(11, filteredDataBins.size());

        // same start/end/special values for all bins

        Assert.assertEquals("<=", filteredDataBins.get(0).getSpecialValue());
        Assert.assertEquals("<=", unfilteredDataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("40.0"), filteredDataBins.get(0).getEnd());
        Assert.assertEquals(new BigDecimal("40.0"), unfilteredDataBins.get(0).getEnd());

        Assert.assertEquals(new BigDecimal("40.0"), filteredDataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("40.0"), unfilteredDataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("45.0"), filteredDataBins.get(1).getEnd());
        Assert.assertEquals(new BigDecimal("45.0"), unfilteredDataBins.get(1).getEnd());

        Assert.assertEquals(new BigDecimal("45.0"), filteredDataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("45.0"), unfilteredDataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("50.0"), filteredDataBins.get(2).getEnd());
        Assert.assertEquals(new BigDecimal("50.0"), unfilteredDataBins.get(2).getEnd());

        Assert.assertEquals(new BigDecimal("50.0"), filteredDataBins.get(3).getStart());
        Assert.assertEquals(new BigDecimal("50.0"), unfilteredDataBins.get(3).getStart());
        Assert.assertEquals(new BigDecimal("55.0"), filteredDataBins.get(3).getEnd());
        Assert.assertEquals(new BigDecimal("55.0"), unfilteredDataBins.get(3).getEnd());

        Assert.assertEquals(new BigDecimal("55.0"), filteredDataBins.get(4).getStart());
        Assert.assertEquals(new BigDecimal("55.0"), unfilteredDataBins.get(4).getStart());
        Assert.assertEquals(new BigDecimal("60.0"), filteredDataBins.get(4).getEnd());
        Assert.assertEquals(new BigDecimal("60.0"), unfilteredDataBins.get(4).getEnd());

        Assert.assertEquals(new BigDecimal("60.0"), filteredDataBins.get(5).getStart());
        Assert.assertEquals(new BigDecimal("60.0"), unfilteredDataBins.get(5).getStart());
        Assert.assertEquals(new BigDecimal("65.0"), filteredDataBins.get(5).getEnd());
        Assert.assertEquals(new BigDecimal("65.0"), unfilteredDataBins.get(5).getEnd());

        Assert.assertEquals(new BigDecimal("85.0"), filteredDataBins.get(10).getStart());
        Assert.assertEquals(new BigDecimal("85.0"), unfilteredDataBins.get(10).getStart());
        Assert.assertEquals(new BigDecimal("90.0"), filteredDataBins.get(10).getEnd());
        Assert.assertEquals(new BigDecimal("90.0"), unfilteredDataBins.get(10).getEnd());

        // same counts until the bin (60-65]

        Assert.assertEquals(2, filteredDataBins.get(0).getCount().intValue());
        Assert.assertEquals(2, unfilteredDataBins.get(0).getCount().intValue());

        Assert.assertEquals(6, filteredDataBins.get(1).getCount().intValue());
        Assert.assertEquals(6, unfilteredDataBins.get(1).getCount().intValue());

        Assert.assertEquals(17, filteredDataBins.get(2).getCount().intValue());
        Assert.assertEquals(17, unfilteredDataBins.get(2).getCount().intValue());

        Assert.assertEquals(24, filteredDataBins.get(3).getCount().intValue());
        Assert.assertEquals(24, unfilteredDataBins.get(3).getCount().intValue());

        Assert.assertEquals(59, filteredDataBins.get(4).getCount().intValue());
        Assert.assertEquals(59, unfilteredDataBins.get(4).getCount().intValue());

        Assert.assertEquals(0, filteredDataBins.get(5).getCount().intValue());
        Assert.assertEquals(53, unfilteredDataBins.get(5).getCount().intValue());

        Assert.assertEquals(0, filteredDataBins.get(6).getCount().intValue());
        Assert.assertEquals(71, unfilteredDataBins.get(6).getCount().intValue());

        Assert.assertEquals(0, filteredDataBins.get(10).getCount().intValue());
        Assert.assertEquals(13, unfilteredDataBins.get(10).getCount().intValue());
    }

    @Test
    public void testLinearDataBinnerWithNumberOfPetOrPetCtScans() {
        String studyId = "genie";
        String attributeId = "N_SCANS_PET_CT_PT";
        String[] values = mockData.get("genie_N_SCANS_PET_CT_PT");

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<DataBin> dataBins = dataBinner.calculateDataBins(
            clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        Assert.assertEquals(4, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(1, dataBins.get(0).getEnd().intValue());

        Assert.assertEquals(2, dataBins.get(1).getStart().intValue());
        Assert.assertEquals(2, dataBins.get(1).getEnd().intValue());

        Assert.assertEquals(3, dataBins.get(2).getStart().intValue());
        Assert.assertEquals(3, dataBins.get(2).getEnd().intValue());

        Assert.assertEquals(">", dataBins.get(3).getSpecialValue());
        Assert.assertEquals(3, dataBins.get(3).getStart().intValue());
    }

    @Test
    public void testLinearDataBinnerWithNumberOfBoneScans() {
        String studyId = "genie";
        String attributeId = "N_SCANS_BONE_PT";
        String[] values = mockData.get("genie_N_SCANS_BONE_PT");

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        // perform data binning with the default distinct value threshold (10)
        List<DataBin> dataBinsWithDefaultThreshold = dataBinner.calculateDataBins(
            clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        Assert.assertEquals(7, dataBinsWithDefaultThreshold.size());

        Assert.assertEquals(0, dataBinsWithDefaultThreshold.get(0).getStart().intValue());
        Assert.assertEquals(0, dataBinsWithDefaultThreshold.get(0).getEnd().intValue());

        Assert.assertEquals(12, dataBinsWithDefaultThreshold.get(6).getStart().intValue());
        Assert.assertEquals(12, dataBinsWithDefaultThreshold.get(6).getEnd().intValue());

        // perform data binning with 5 as the distinct value threshold
        List<DataBin> dataBinsWithThreshold5 = dataBinner.calculateDataBins(
            clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds, 5);

        Assert.assertEquals(2, dataBinsWithThreshold5.size());

        Assert.assertEquals(0, dataBinsWithThreshold5.get(0).getStart().intValue());
        Assert.assertEquals(0, dataBinsWithThreshold5.get(0).getEnd().intValue());

        Assert.assertEquals(">", dataBinsWithThreshold5.get(1).getSpecialValue());
        Assert.assertEquals(0, dataBinsWithThreshold5.get(1).getStart().intValue());
        Assert.assertNull(dataBinsWithThreshold5.get(1).getEnd());
    }

    @Test
    public void testLinearDataBinnerWithAgeAtWhichSequencingWasReported() {
        String studyId = "genie_public";
        String attributeId = "AGE_AT_WHICH_SEQUENCING_WAS_REPORTED";
        String[] values = mockData.get("genie_public_AGE_AT_WHICH_SEQUENCING_WAS_REPORTED");

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setBinMethod(BinMethod.CUSTOM);
        clinicalDataBinFilter.setCustomBins(
            Stream.of(45.0, 60.0, 70.0).map(BigDecimal::valueOf).collect(Collectors.toList())
        );

        List<DataBin> dataBins1 = dataBinner.calculateDataBins(
            clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);
        Assert.assertEquals(5, dataBins1.size());

        Assert.assertEquals("<=", dataBins1.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("45.0"), dataBins1.get(0).getEnd());
        Assert.assertEquals(337, dataBins1.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("45.0"), dataBins1.get(1).getStart());
        Assert.assertEquals(new BigDecimal("60.0"), dataBins1.get(1).getEnd());
        Assert.assertEquals(118, dataBins1.get(1).getCount().intValue());

        Assert.assertEquals(new BigDecimal("60.0"), dataBins1.get(2).getStart());
        Assert.assertEquals(new BigDecimal("70.0"), dataBins1.get(2).getEnd());
        Assert.assertEquals(64, dataBins1.get(2).getCount().intValue());

        Assert.assertEquals(">", dataBins1.get(3).getSpecialValue());
        Assert.assertEquals(new BigDecimal("70.0"), dataBins1.get(3).getStart());
        Assert.assertEquals(223, dataBins1.get(3).getCount().intValue());

        Assert.assertEquals("Unknown", dataBins1.get(4).getSpecialValue());
        Assert.assertEquals(10, dataBins1.get(4).getCount().intValue());


        clinicalDataBinFilter.setCustomBins(
            Stream.of(40.0, 55.0).map(BigDecimal::valueOf).collect(Collectors.toList())
        );

        List<DataBin> dataBins2 = dataBinner.calculateDataBins(
            clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);
        Assert.assertEquals(4, dataBins2.size());

        Assert.assertEquals("<=", dataBins2.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("40.0"), dataBins2.get(0).getEnd());
        Assert.assertEquals(305, dataBins2.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("40.0"), dataBins2.get(1).getStart());
        Assert.assertEquals(new BigDecimal("55.0"), dataBins2.get(1).getEnd());
        Assert.assertEquals(112, dataBins2.get(1).getCount().intValue());

        Assert.assertEquals(">", dataBins2.get(2).getSpecialValue());
        Assert.assertEquals(new BigDecimal("55.0"), dataBins2.get(2).getStart());
        Assert.assertEquals(325, dataBins2.get(2).getCount().intValue());

        Assert.assertEquals("Unknown", dataBins2.get(3).getSpecialValue());
        Assert.assertEquals(10, dataBins2.get(3).getCount().intValue());


        clinicalDataBinFilter.setCustomBins(
            Stream.of(5.0, 10.0, 90.0, 100.0).map(BigDecimal::valueOf).collect(Collectors.toList())
        );

        List<DataBin> dataBins3 = dataBinner.calculateDataBins(
            clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);
        Assert.assertEquals(4, dataBins3.size());

        // even if we have 5 and 10 they will not be used because we have special values like "<18"
        Assert.assertEquals("<=", dataBins3.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("18"), dataBins3.get(0).getEnd());
        Assert.assertEquals(156, dataBins3.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("18"), dataBins3.get(1).getStart());
        Assert.assertEquals(new BigDecimal("89"), dataBins3.get(1).getEnd());
        Assert.assertEquals(569, dataBins3.get(1).getCount().intValue());

        // even if we have 90 and 100 they will not be used because we have special values like ">89" in our data
        Assert.assertEquals(">", dataBins3.get(2).getSpecialValue());
        Assert.assertEquals(new BigDecimal("89"), dataBins3.get(2).getStart());
        Assert.assertEquals(17, dataBins3.get(2).getCount().intValue());

        Assert.assertEquals("Unknown", dataBins3.get(3).getSpecialValue());
        Assert.assertEquals(10, dataBins3.get(3).getCount().intValue());
    }

    @Test
    public void testLinearDataBinnerWithPediatricAge() {
        String studyId = "skcm_broad";
        String attributeId = "AGE_AT_PROCUREMENT";
        String[] values = mockData.get("skcm_broad_AGE_AT_PROCUREMENT");
        String[] patientsWithNoClinicalData = {
            "NA_PATIENT_01", "NA_PATIENT_02", "NA_PATIENT_03", "NA_PATIENT_04"
        };
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setBinMethod(BinMethod.CUSTOM);
        clinicalDataBinFilter.setCustomBins(Arrays.asList(18.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0).stream().map(item -> BigDecimal.valueOf(item)).collect(Collectors.toList()));

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);
        patientIds.addAll(Arrays.asList(patientsWithNoClinicalData));

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);
        
        Assert.assertEquals(10, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("18.0"), dataBins.get(0).getEnd());
        Assert.assertEquals(1, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("18.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("20.0"), dataBins.get(1).getEnd());
        Assert.assertEquals(0, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new BigDecimal("20.0"), dataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("30.0"), dataBins.get(2).getEnd());
        Assert.assertEquals(9, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new BigDecimal("30.0"), dataBins.get(3).getStart());
        Assert.assertEquals(new BigDecimal("40.0"), dataBins.get(3).getEnd());
        Assert.assertEquals(16, dataBins.get(3).getCount().intValue());

        Assert.assertEquals(new BigDecimal("40.0"), dataBins.get(4).getStart());
        Assert.assertEquals(new BigDecimal("50.0"), dataBins.get(4).getEnd());
        Assert.assertEquals(31, dataBins.get(4).getCount().intValue());

        Assert.assertEquals(new BigDecimal("50.0"), dataBins.get(5).getStart());
        Assert.assertEquals(new BigDecimal("60.0"), dataBins.get(5).getEnd());
        Assert.assertEquals(25, dataBins.get(5).getCount().intValue());

        Assert.assertEquals(new BigDecimal("60.0"), dataBins.get(6).getStart());
        Assert.assertEquals(new BigDecimal("70.0"), dataBins.get(6).getEnd());
        Assert.assertEquals(24, dataBins.get(6).getCount().intValue());

        Assert.assertEquals(new BigDecimal("70.0"), dataBins.get(7).getStart());
        Assert.assertEquals(new BigDecimal("80.0"), dataBins.get(7).getEnd());
        Assert.assertEquals(11, dataBins.get(7).getCount().intValue());

        Assert.assertEquals(new BigDecimal("80.0"), dataBins.get(8).getStart());
        Assert.assertEquals(new BigDecimal("90.0"), dataBins.get(8).getEnd());
        Assert.assertEquals(2, dataBins.get(8).getCount().intValue());

        Assert.assertEquals("NA", dataBins.get(9).getSpecialValue());
        Assert.assertEquals(4, dataBins.get(9).getCount().intValue());
    }

    @Test
    public void testLinearDataBinnerWithPediatricAgeCustomBinsTest1() {
        String studyId = "skcm_broad";
        String attributeId = "AGE_AT_PROCUREMENT";
        String[] values = mockData.get("skcm_broad_AGE_AT_PROCUREMENT");
        String[] patientsWithNoClinicalData = {
            "NA_PATIENT_01", "NA_PATIENT_02", "NA_PATIENT_03", "NA_PATIENT_04"
        };
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setBinMethod(BinMethod.CUSTOM);
        clinicalDataBinFilter.setCustomBins(Arrays.asList(18.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0).stream().map(item -> BigDecimal.valueOf(item)).collect(Collectors.toList()));

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);
        patientIds.addAll(Arrays.asList(patientsWithNoClinicalData));

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        List<DataBin> expected = Arrays.asList(
            createDataBin("<=", null, "18.0", 1),
            createDataBin(null, "18.0", "20.0", 0),
            createDataBin(null, "20.0", "30.0", 9),
            createDataBin(null, "30.0", "40.0", 16),
            createDataBin(null, "40.0", "50.0", 31),
            createDataBin(null, "50.0", "60.0", 25),
            createDataBin(null, "60.0", "70.0", 24),
            createDataBin(null, "70.0", "80.0", 11),
            createDataBin(null, "80.0", "90.0", 2),
            createDataBin("NA", null, null, 4)
        );

        testBinsIdentical(expected, dataBins);
    }

    @Test
    public void testLinearDataBinnerWithPediatricAgeCustomBinsTest2() {
        String studyId = "skcm_broad";
        String attributeId = "AGE_AT_PROCUREMENT";
        String[] values = mockData.get("skcm_broad_AGE_AT_PROCUREMENT");
        String[] patientsWithNoClinicalData = {
            "NA_PATIENT_01", "NA_PATIENT_02", "NA_PATIENT_03", "NA_PATIENT_04"
        };
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setBinMethod(BinMethod.CUSTOM);
        clinicalDataBinFilter.setCustomBins(Arrays.asList(18.0, 25.0, 30.0, 35.0).stream().map(item -> BigDecimal.valueOf(item)).collect(Collectors.toList()));

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);
        patientIds.addAll(Arrays.asList(patientsWithNoClinicalData));

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);


        List<DataBin> expected = Arrays.asList(
            createDataBin("<=", null, "18.0", 1),
            createDataBin(null, "18.0", "25.0", 2),
            createDataBin(null, "25.0", "30.0", 7),
            createDataBin(null, "30.0", "35.0", 7),
            createDataBin(">", "35.0", null, 102),
            createDataBin("NA", null, null, 4)
        );

        testBinsIdentical(expected, dataBins);
    }

    @Test
    public void testLinearDataBinnerWithPediatricAgeCustomBinsSingleBoundary() {
        String studyId = "skcm_broad";
        String attributeId = "AGE_AT_PROCUREMENT";
        String[] values = mockData.get("skcm_broad_AGE_AT_PROCUREMENT");
        String[] patientsWithNoClinicalData = {
            "NA_PATIENT_01", "NA_PATIENT_02", "NA_PATIENT_03", "NA_PATIENT_04"
        };
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setBinMethod(BinMethod.CUSTOM);
        clinicalDataBinFilter.setCustomBins(Arrays.asList(30.0).stream().map(item -> BigDecimal.valueOf(item)).collect(Collectors.toList()));

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);
        patientIds.addAll(Arrays.asList(patientsWithNoClinicalData));

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);


        List<DataBin> expected = Arrays.asList(
            createDataBin("<=", null, "30.0", 10),
            createDataBin(">", "30.0", null, 109),
            createDataBin("NA", null, null, 4)
        );

        testBinsIdentical(expected, dataBins);
    }


    @Test
    public void testLinearDataBinnerWithPediatricAgeCustomBinsTwoBoundaries() {
        String studyId = "skcm_broad";
        String attributeId = "AGE_AT_PROCUREMENT";
        String[] values = mockData.get("skcm_broad_AGE_AT_PROCUREMENT");
        String[] patientsWithNoClinicalData = {
            "NA_PATIENT_01", "NA_PATIENT_02", "NA_PATIENT_03", "NA_PATIENT_04"
        };
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setBinMethod(BinMethod.CUSTOM);
        clinicalDataBinFilter.setCustomBins(Arrays.asList(20.0, 50.0).stream().map(item -> BigDecimal.valueOf(item)).collect(Collectors.toList()));

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);
        patientIds.addAll(Arrays.asList(patientsWithNoClinicalData));

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);


        List<DataBin> expected = Arrays.asList(
            createDataBin("<=", null, "20.0", 1),
            createDataBin(null, "20.0", "50.0", 56),
            createDataBin(">", "50.0", null, 62),
            createDataBin("NA", null, null, 4)
        );

        testBinsIdentical(expected, dataBins);
    }

    @Test
    public void testLinearDataBinnerWithPediatricAgeGenerateBins() {
        String studyId = "skcm_broad";
        String attributeId = "AGE_AT_PROCUREMENT";
        String[] values = mockData.get("skcm_broad_AGE_AT_PROCUREMENT");
        String[] patientsWithNoClinicalData = {
            "NA_PATIENT_01", "NA_PATIENT_02", "NA_PATIENT_03", "NA_PATIENT_04"
        };
        
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setBinMethod(BinMethod.GENERATE);
        BinsGeneratorConfig generateBins = new BinsGeneratorConfig();
        generateBins.setBinSize(new BigDecimal(10));
        generateBins.setAnchorValue(new BigDecimal(50));
        clinicalDataBinFilter.setBinsGeneratorConfig(generateBins);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);
        patientIds.addAll(Arrays.asList(patientsWithNoClinicalData));

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);
        
        List<DataBin> expected = Arrays.asList(
            createDataBin("<=", null, "20.0", 1),
            createDataBin(null, "20.0", "30.0", 9),
            createDataBin(null, "30.0", "40.0", 16),
            createDataBin(null, "40.0", "50.0", 31),
            createDataBin(null, "50.0", "60.0", 25),
            createDataBin(null, "60.0", "70.0", 24),
            createDataBin(null, "70.0", "80.0", 11),
            createDataBin(">", "80.0", null, 2),
            createDataBin("NA", null, null, 4)
        );

        testBinsIdentical(expected, dataBins);
    }

    @Test
    public void testLinearDataBinnerWithPediatricAgeMedianBins() {
        String studyId = "skcm_broad";
        String attributeId = "AGE_AT_PROCUREMENT";
        String[] values = mockData.get("skcm_broad_AGE_AT_PROCUREMENT");
        String[] patientsWithNoClinicalData = {
            "NA_PATIENT_01", "NA_PATIENT_02", "NA_PATIENT_03", "NA_PATIENT_04"
        };

        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setBinMethod(BinMethod.MEDIAN);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);
        patientIds.addAll(Arrays.asList(patientsWithNoClinicalData));

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        List<DataBin> expected = Arrays.asList(
            createDataBin("<=", null, "51.0", 61),
            createDataBin(">", "51.0", null, 58),
            createDataBin("NA", null, null, 4)
        );
        
        testBinsIdentical(expected, dataBins);
    }

    @Test
    public void testLinearDataBinnerWithPediatricAgeQuartileBins() {
        String studyId = "skcm_broad";
        String attributeId = "AGE_AT_PROCUREMENT";
        String[] values = mockData.get("skcm_broad_AGE_AT_PROCUREMENT");
        String[] patientsWithNoClinicalData = {
            "NA_PATIENT_01", "NA_PATIENT_02", "NA_PATIENT_03", "NA_PATIENT_04"
        };

        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setBinMethod(BinMethod.QUARTILE);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);
        patientIds.addAll(Arrays.asList(patientsWithNoClinicalData));

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        List<DataBin> expected = Arrays.asList(
            createDataBin("<=", null, "41.0", 30),
            createDataBin(null, "41.0", "51.0", 31),
            createDataBin(null, "51.0", "65.0", 33),
            createDataBin(">", "65.0", null, 25),
            createDataBin("NA", null, null, 4)
        );
        
        testBinsIdentical(expected, dataBins);
    }

    @Test
    public void testLinearDataBinnerQuartileBinsLowComplexitySeriesQ1Q2Q3Identical() {
        String studyId = "skcm_broad";
        String attributeId = "AGE_AT_PROCUREMENT";
        String[] values = mockData.get("q1q2q3_identical");

        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setBinMethod(BinMethod.QUARTILE);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        List<DataBin> expected = Arrays.asList(
            createDataBin("<=", null ,"250", 50)
        );

        testBinsIdentical(expected, dataBins);
    }

    @Test
    public void testLinearDataBinnerQuartileBinsLowComplexitySeriesQ2Q3Identical() {
        String studyId = "skcm_broad";
        String attributeId = "AGE_AT_PROCUREMENT";
        String[] values = mockData.get("q2q3_identical");

        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setBinMethod(BinMethod.QUARTILE);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        List<DataBin> expected = Arrays.asList(
            createDataBin("<=", null, "15", 6),
            createDataBin(null, "15", "250", 17)
        );

        testBinsIdentical(expected, dataBins);
    }

    @Test
    public void testLinearDataBinnerWithBigNumbers() {
        String studyId = "random_";
        String attributeId = "BIG_NUMBER";
        String[] values = mockData.get("random_BIG_NUMBER");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        List<DataBin> expected = Arrays.asList(
            createDataBin(null, "1.0E+8", "316227766", 2),
            createDataBin(null, "316227766", "1.0E+9", 9),
            createDataBin(null, "1.0E+9", "3.16227766E+9", 25),
            createDataBin(null, "3.16227766E+9", "1.0E+10", 9)
        );

        testBinsIdentical(expected, dataBins);
    }

    @Test
    public void testLinearDataBinnerWithPredefinedAttribute() {
        String studyId = "crc_msk_2018";
        String attributeId = "MSI_SCORE";
        String[] values = mockData.get("crc_msk_2018_MSI_SCORE");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setBinMethod(BinMethod.CUSTOM);
        clinicalDataBinFilter.setCustomBins(Arrays.asList(1.0, 2.0, 5.0, 10.0, 30.0).stream().map(item -> BigDecimal.valueOf(item)).collect(Collectors.toList()));

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> sampleIds = getCaseIds(clinicalData, false);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.SAMPLE, clinicalData, sampleIds);

        List<DataBin> expected = Arrays.asList(
            createDataBin("<=", null, "1.0", 722),
            createDataBin(null, "1.0", "2.0", 203),
            createDataBin(null, "2.0", "5.0", 93),
            createDataBin(null, "5.0", "10.0", 12),
            createDataBin(null, "10.0", "30.0", 39),
            createDataBin(">", "30.0", null, 57),
            createDataBin("NA", null, null, 8)
        );

        testBinsIdentical(expected, dataBins);
    }

    @Test
    public void testLinearDataBinnerWithNA() {
        String studyId = "blca_tcga";
        String attributeId = "LYMPH_NODE_EXAMINED_COUNT";
        String[] values = mockData.get("blca_tcga_LYMPH_NODE_EXAMINED_COUNT");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        List<DataBin> expected = Arrays.asList(
            createDataBin("<=", null, "5.0", 30),
            createDataBin(null, "5.0", "10.0", 41),
            createDataBin(null, "10.0", "15.0", 55),
            createDataBin(null, "15.0", "20.0", 39),
            createDataBin(null, "20.0", "25.0", 26),
            createDataBin(null, "25.0", "30.0", 32),
            createDataBin(null, "30.0", "35.0", 8),
            createDataBin(null, "35.0", "40.0", 11),
            createDataBin(null, "40.0", "45.0", 8),
            createDataBin(null, "45.0", "50.0", 9),
            createDataBin(null, "50.0", "55.0", 6),
            createDataBin(null, "55.0", "60.0", 4),
            createDataBin(null, "60.0", "65.0", 6),
            createDataBin(">", "65.0", null, 28),
            createDataBin("NA", null, null, 109)
        );

        testBinsIdentical(expected, dataBins);
    }

    @Test
    public void testLinearDataBinnerWithZeroIQR() {
        String studyId = "impact";
        String attributeId = "DNA_INPUT";
        String[] values = mockData.get("impact_DNA_INPUT");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> sampleIds = getCaseIds(clinicalData, false);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.SAMPLE, clinicalData, sampleIds);

        Assert.assertEquals(19, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("60.0"), dataBins.get(0).getEnd());

        Assert.assertEquals(new BigDecimal("60.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("70.0"), dataBins.get(1).getEnd());

        Assert.assertEquals(new BigDecimal("230.0"), dataBins.get(18).getStart());
        Assert.assertEquals(new BigDecimal("240.0"), dataBins.get(18).getEnd());
    }

    @Test
    public void testLinearDataBinnerWithAlwaysZeroIQR() {
        String studyId = "unknown";
        String attributeId = "DNA_INPUT";
        String[] values = mockData.get("recursively_always_zero_IQR");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> sampleIds = getCaseIds(clinicalData, false);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.SAMPLE, clinicalData, sampleIds, 5);

        Assert.assertEquals(1, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("66"), dataBins.get(0).getEnd());
        Assert.assertEquals("all values should be included in a single bin",
            values.length, dataBins.get(0).getCount().intValue());
    }

    @Test
    public void testDiscreteDataBinner() {
        String studyId = "acyc_fmi_2014";
        String attributeId = "ACTIONABLE_ALTERATIONS";
        String[] values = mockData.get("acyc_fmi_2014_ACTIONABLE_ALTERATIONS");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> sampleIds = getCaseIds(clinicalData, false);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.SAMPLE, clinicalData, sampleIds);

        Assert.assertEquals(5, dataBins.size());

        Assert.assertEquals(new BigDecimal("0.0"), dataBins.get(0).getStart());
        Assert.assertEquals(new BigDecimal("0.0"), dataBins.get(0).getEnd());
        Assert.assertEquals(16, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("1.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("1.0"), dataBins.get(1).getEnd());
        Assert.assertEquals(6, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new BigDecimal("2.0"), dataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("2.0"), dataBins.get(2).getEnd());
        Assert.assertEquals(4, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new BigDecimal("3.0"), dataBins.get(3).getStart());
        Assert.assertEquals(new BigDecimal("3.0"), dataBins.get(3).getEnd());
        Assert.assertEquals(1, dataBins.get(3).getCount().intValue());

        Assert.assertEquals(new BigDecimal("5.0"), dataBins.get(4).getStart());
        Assert.assertEquals(new BigDecimal("5.0"), dataBins.get(4).getEnd());
        Assert.assertEquals(1, dataBins.get(4).getCount().intValue());
    }

    @Test
    public void testScientificDataBinner() {
        String studyId = "blca_dfarber_mskcc_2014";
        String attributeId = "SILENT_RATE";
        String[] values = mockData.get("blca_dfarber_mskcc_2014_SILENT_RATE");
        String[] samplesWithNoClinicalData = {
            "NA_SAMPLE_01", "NA_SAMPLE_02", "NA_SAMPLE_03", "NA_SAMPLE_04", "NA_SAMPLE_05", "NA_SAMPLE_06"
        };
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> sampleIds = getCaseIds(clinicalData, false);
        sampleIds.addAll(Arrays.asList(samplesWithNoClinicalData));

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.SAMPLE, clinicalData, sampleIds);

        Assert.assertEquals(5, dataBins.size());

        Assert.assertEquals(new BigDecimal("1.0e-8"), dataBins.get(0).getStart());
        Assert.assertEquals(new BigDecimal("1.0e-7"), dataBins.get(0).getEnd());
        Assert.assertEquals(1, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("1.0e-7"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("1.0e-6"), dataBins.get(1).getEnd());
        Assert.assertEquals(16, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new BigDecimal("1.0e-6"), dataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("1.0e-5").compareTo(dataBins.get(2).getEnd().round(new MathContext(5, RoundingMode.CEILING))), 0);
        Assert.assertEquals(32, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(">", dataBins.get(3).getSpecialValue());
        Assert.assertEquals(new BigDecimal("1.0e-5").compareTo(dataBins.get(3).getStart().round(new MathContext(5, RoundingMode.CEILING))), 0);
        Assert.assertEquals(1, dataBins.get(3).getCount().intValue());

        Assert.assertEquals("NA", dataBins.get(4).getSpecialValue());
        Assert.assertEquals(3 + 6, dataBins.get(4).getCount().intValue());
    }

    @Test
    public void testLogScaleNoOutlierDataBinner() {
        String studyId = "llg_tcga";
        String attributeId = "egfr_mrna_expression";
        String[] values = mockData.get("llg_tcga_egfr_mrna_expression");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> sampleIds = getCaseIds(clinicalData, false);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.SAMPLE, clinicalData, sampleIds);

        Assert.assertEquals(8, dataBins.size());

        Assert.assertEquals(new BigDecimal("31.0"), dataBins.get(0).getStart());
        Assert.assertEquals(new BigDecimal("100.0"), dataBins.get(0).getEnd());
        Assert.assertEquals(6, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("100.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("316.0"), dataBins.get(1).getEnd());
        Assert.assertEquals(25, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new BigDecimal("316.0"), dataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("1000.0"), dataBins.get(2).getEnd());
        Assert.assertEquals(84, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new BigDecimal("1000.0"), dataBins.get(3).getStart());
        Assert.assertEquals(new BigDecimal("3162.0"), dataBins.get(3).getEnd());
        Assert.assertEquals(222, dataBins.get(3).getCount().intValue());

        Assert.assertEquals(new BigDecimal("3162.0"), dataBins.get(4).getStart());
        Assert.assertEquals(new BigDecimal("10000.0"), dataBins.get(4).getEnd());
        Assert.assertEquals(148, dataBins.get(4).getCount().intValue());

        Assert.assertEquals(new BigDecimal("10000.0"), dataBins.get(5).getStart());
        Assert.assertEquals(new BigDecimal("31622.0"), dataBins.get(5).getEnd());
        Assert.assertEquals(30, dataBins.get(5).getCount().intValue());

        Assert.assertEquals(new BigDecimal("31622.0"), dataBins.get(6).getStart());
        Assert.assertEquals(new BigDecimal("100000.0"), dataBins.get(6).getEnd());
        Assert.assertEquals(13, dataBins.get(6).getCount().intValue());

        Assert.assertEquals(">", dataBins.get(7).getSpecialValue());
        Assert.assertEquals(new BigDecimal("100000.0"), dataBins.get(7).getStart());
        Assert.assertEquals(2, dataBins.get(7).getCount().intValue());
    }

    @Test
    public void testLogScaleDataBinner() {
        String studyId = "ampca_bcm_2016";
        String attributeId = "DAYS_TO_LAST_FOLLOWUP";
        String[] values = mockData.get("ampca_bcm_2016_DAYS_TO_LAST_FOLLOWUP");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        Assert.assertEquals(7, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("10.0"), dataBins.get(0).getEnd());
        Assert.assertEquals(1, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("10.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("31.0"), dataBins.get(1).getEnd());
        Assert.assertEquals(3, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new BigDecimal("31.0"), dataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("100.0"), dataBins.get(2).getEnd());
        Assert.assertEquals(5, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new BigDecimal("100.0"), dataBins.get(3).getStart());
        Assert.assertEquals(new BigDecimal("316.0"), dataBins.get(3).getEnd());
        Assert.assertEquals(23, dataBins.get(3).getCount().intValue());

        Assert.assertEquals(new BigDecimal("316.0"), dataBins.get(4).getStart());
        Assert.assertEquals(new BigDecimal("1000.0"), dataBins.get(4).getEnd());
        Assert.assertEquals(67, dataBins.get(4).getCount().intValue());

        Assert.assertEquals(new BigDecimal("1000.0"), dataBins.get(5).getStart());
        Assert.assertEquals(new BigDecimal("3162.0"), dataBins.get(5).getEnd());
        Assert.assertEquals(55, dataBins.get(5).getCount().intValue());

        Assert.assertEquals(new BigDecimal("3162.0"), dataBins.get(6).getStart());
        Assert.assertEquals(new BigDecimal("10000.0"), dataBins.get(6).getEnd());
        Assert.assertEquals(6, dataBins.get(6).getCount().intValue());
    }

    @Test
    public void testLogScaleDisabledDataBinner() {
        String studyId = "ampca_bcm_2016";
        String attributeId = "DAYS_TO_LAST_FOLLOWUP";
        String[] values = mockData.get("ampca_bcm_2016_DAYS_TO_LAST_FOLLOWUP");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setDisableLogScale(true);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        Assert.assertEquals(17, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("200.0"), dataBins.get(0).getEnd());
        Assert.assertEquals(17, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("200.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("400.0"), dataBins.get(1).getEnd());
        Assert.assertEquals(24, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new BigDecimal("3200.0"), dataBins.get(16).getStart());
        Assert.assertEquals(">", dataBins.get(16).getSpecialValue());
        Assert.assertEquals(6, dataBins.get(16).getCount().intValue());
    }

    @Test
    public void testNegativeLogScaleDataBinner() {
        String studyId = "acc_tcga";
        String attributeId = "DAYS_TO_BIRTH";
        String[] values = mockData.get("acc_tcga_DAYS_TO_BIRTH");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        Assert.assertEquals(2, dataBins.size());

        Assert.assertEquals(new BigDecimal("-31622.0"), dataBins.get(0).getStart());
        Assert.assertEquals(new BigDecimal("-10000.0"), dataBins.get(0).getEnd());
        Assert.assertEquals(78, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("-10000.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("-3162.0"), dataBins.get(1).getEnd());
        Assert.assertEquals(14, dataBins.get(1).getCount().intValue());
    }

    @Test
    public void testLogScaleDataBinnerWithSpecialOutliers() {
        String studyId = "genie";
        String attributeId = "INT_DOD";
        String[] values = mockData.get("genie_INT_DOD");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        Assert.assertEquals(5, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("6570.0"), dataBins.get(0).getEnd());
        Assert.assertEquals(8, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new BigDecimal("6570.0"), dataBins.get(1).getStart());
        Assert.assertEquals(new BigDecimal("10000.0"), dataBins.get(1).getEnd());

        Assert.assertEquals(new BigDecimal("10000.0"), dataBins.get(2).getStart());
        Assert.assertEquals(new BigDecimal("31622.0"), dataBins.get(2).getEnd());

        Assert.assertEquals(new BigDecimal("31622.0"), dataBins.get(3).getStart());
        Assert.assertEquals(new BigDecimal("32485.0"), dataBins.get(3).getEnd());

        Assert.assertEquals(new BigDecimal("32485.0"), dataBins.get(4).getStart());
        Assert.assertEquals(">", dataBins.get(4).getSpecialValue());
        Assert.assertEquals(5, dataBins.get(4).getCount().intValue());
    }

    @Test
    public void testNegativeLogScaleDisabledDataBinner() {
        String studyId = "acc_tcga";
        String attributeId = "DAYS_TO_BIRTH";
        String[] values = mockData.get("acc_tcga_DAYS_TO_BIRTH");
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(attributeId);
        clinicalDataBinFilter.setDisableLogScale(true);

        List<Binnable> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = getCaseIds(clinicalData, true);

        List<DataBin> dataBins = dataBinner.calculateDataBins(clinicalDataBinFilter, ClinicalDataType.PATIENT, clinicalData, patientIds);

        Assert.assertEquals(14, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new BigDecimal("-30000.0"), dataBins.get(0).getEnd());

        Assert.assertEquals(new BigDecimal("-6000.0"), dataBins.get(13).getStart());
        Assert.assertEquals(">", dataBins.get(13).getSpecialValue());
    }

    private List<Binnable> mockClinicalData(String attributeId, String studyId, String[] values) {
        List<Binnable> clinicalDataList =  new ArrayList<>();

        for (int index = 0; index < values.length; index++)
        {
            ClinicalData clinicalData = new ClinicalData();

            clinicalData.setAttrId(attributeId);
            clinicalData.setStudyId(studyId);
            clinicalData.setSampleId("sample_" + index);
            clinicalData.setPatientId("patient_" + index);
            clinicalData.setAttrValue(values[index]);

            clinicalDataList.add(clinicalData);
        }

        return clinicalDataList;
    }

    private DataBin createDataBin(String specialValue, String start, String end, int count) {
        DataBin dataBin = new DataBin();
        dataBin.setCount(count);
        if (specialValue != null) {
            dataBin.setSpecialValue(specialValue);
        }
        if (start != null) {
            dataBin.setStart(new BigDecimal(start));
        }
        if (end != null) {
            dataBin.setEnd(new BigDecimal(end));
        }
        return dataBin;
    }
    
    private void testBinsIdentical(List<DataBin> expected, List<DataBin> observed) {
        Assert.assertEquals(expected.size(), observed.size());
        IntStream.range(0, expected.size()).forEach(i -> {
            DataBin e = expected.get(i);
            DataBin o = observed.get(i);
            Assert.assertTrue("Element " + i + " is not correct.", new ReflectionEquals(e).matches(o));
        });
    }
}
