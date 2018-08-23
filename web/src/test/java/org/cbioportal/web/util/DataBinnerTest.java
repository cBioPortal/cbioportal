package org.cbioportal.web.util;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DataBin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class DataBinnerTest
{
    private Map<String, String[]> mockData;
    
    private DataBinner dataBinner;

    @Before
    public void setup() {
        StudyViewFilterUtil studyViewFilterUtil = new StudyViewFilterUtil();
        DataBinHelper dataBinHelper = new DataBinHelper(studyViewFilterUtil);
        DiscreteDataBinner discreteDataBinner = new DiscreteDataBinner(dataBinHelper);
        LinearDataBinner linearDataBinner = new LinearDataBinner(dataBinHelper);
        ScientificSmallDataBinner scientificSmallDataBinner = new ScientificSmallDataBinner(dataBinHelper);
        LogScaleDataBinner logScaleDataBinner = new LogScaleDataBinner(dataBinHelper);
        
        dataBinner = new DataBinner(
            dataBinHelper, discreteDataBinner, linearDataBinner, scientificSmallDataBinner, logScaleDataBinner);
        
        mockData = initMockData();
    }
    
    public Map<String, String[]> initMockData()
    {
        Map<String, String[]> mockData = new LinkedHashMap<>();
        
        mockData.put("blca_tcga_AGE", new String[] {
            "34","37","41","42","43","44","45","45","46","47","47","47","47","48","48","48","48","48","48","49","49",
            "49","50","50","50","51","52","52","52","52","52","52","53","53","53","53","54","54","54","54","54","54",
            "54","55","55","55","55","55","55","56","56","56","56","56","56","56","56","56","57","57","57","57","57",
            "57","57","57","57","57","58","58","58","58","58","58","59","59","59","59","59","59","59","59","59","59",
            "59","59","59","59","60","60","60","60","60","60","60","60","60","60","60","60","60","60","60","60","60",
            "60","60","60","61","61","61","61","61","61","61","61","61","61","61","61","62","62","62","62","62","62",
            "62","62","63","63","63","63","63","63","63","63","63","64","64","64","64","64","64","64","64","64","64",
            "64","64","64","65","65","65","65","65","65","65","65","65","65","65","66","66","66","66","66","66","66",
            "66","66","66","66","66","66","66","66","66","66","67","67","67","67","67","67","67","67","67","67","67",
            "67","67","68","68","68","68","68","68","68","68","68","68","68","68","69","69","69","69","69","69","69",
            "69","69","69","69","69","69","70","70","70","70","70","70","70","70","70","70","70","70","70","70","70",
            "70","71","71","71","71","71","71","71","71","72","72","72","72","72","72","72","72","72","72","73","73",
            "73","73","73","73","73","73","73","73","73","73","73","73","73","73","74","74","74","74","74","74","74",
            "74","74","74","74","74","75","75","75","75","75","75","75","75","75","75","75","75","75","75","75","75",
            "75","75","75","75","75","76","76","76","76","76","76","76","76","76","76","76","76","76","76","76","77",
            "77","77","77","77","77","77","77","77","77","77","77","78","78","78","78","78","78","78","78","78","78",
            "78","78","79","79","79","79","79","79","79","79","79","79","79","79","79","80","80","80","80","80","80",
            "80","80","80","80","80","80","81","81","81","81","81","81","81","81","82","82","82","82","82","82","82",
            "82","83","83","83","83","83","83","83","84","84","84","84","84","84","84","85","85","85","85","85","86",
            "86","86","87","87","87","87","87","88","89","90","90","90"
        });

        mockData.put("skcm_broad_AGE_AT_PROCUREMENT", new String[] {
            "72","22","55","57","70","21","67","42","26","54","39","67","46","52","47","53","51","35","53","46","42",
            "31","83","29","52","47","41","35","45","40","50","37","54","71","49","49","56","74","48","48","76","35",
            "67","74","63","47","67","39","31","62","58","65","72","27","59","87","69","41","39","53","17","44","76",
            "51","46","63","41","38","34","69","63","65","59","58","60","56","65","26","58","48","65","44","45","28",
            "46","48","52","52","70","57","63","68","45","51","39","79","53","41","47","39","49","42","37","78","69",
            "33","71","80","49","65","67","70","46","49","63","28","62","27","51"
        });

        mockData.put("blca_tcga_LYMPH_NODE_EXAMINED_COUNT", new String[] {
            "170","141","140","112","111","108","107","104","93","87","86","85","83","79","77","77","76","75","74","73",
            "73","71","71","70","69","68","67","66","65","64","64","62","61","61","60","58","58","57","54","54","53","53",
            "52","51","48","48","48","47","47","47","47","46","46","45","45","44","43","41","41","41","41","40","40","40",
            "38","38","38","37","37","36","36","36","35","34","34","34","34","33","31","31","30","30","30","30","30","29",
            "29","29","28","28","28","28","28","28","28","28","28","28","27","27","27","27","27","27","27","27","27","27",
            "26","26","26","26","25","25","25","25","25","24","24","24","24","24","24","23","23","23","23","23","22","22",
            "22","22","21","21","21","21","21","21","20","20","20","20","19","19","19","19","19","19","19","19","18","18",
            "18","18","18","18","18","18","18","17","17","17","17","17","17","17","16","16","16","16","16","16","16","16",
            "16","16","16","15","15","15","15","15","15","15","15","14","14","14","14","14","14","14","14","14","14","14",
            "14","14","14","14","14","14","14","14","13","13","13","13","13","13","13","13","12","12","12","12","12","12",
            "12","12","12","11","11","11","11","11","11","11","11","11","11","11","10","10","10","10","10","9","9","9",
            "9","9","8","8","8","8","8","8","8","8","8","8","8","8","8","8","8","7","7","7","7","7","7","7","7","6","6",
            "6","6","6","6","6","6","5","5","5","5","5","5","5","5","5","4","4","4","4","4","3","3","3","2","2","2","2",
            "2","2","2","2","2","2","2","1","0","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA",
            "NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA",
            "NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA",
            "NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA",
            "NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA","NA",
            "NA","NA","NA","NA","NA","NA"
        });

        mockData.put("acyc_fmi_2014_ACTIONABLE_ALTERATIONS", new String[] {
            "1","0","0","2","2","2","1","0","0","1","0","0","0","5","0","0","1","0","2","0","3","0","0","0","1","0","0","1"
        });

        mockData.put("blca_dfarber_mskcc_2014_SILENT_RATE", new String[] {
            "2.87E-06","1.92E-06","1.32E-06","1.78E-06","4.93E-06","3.01E-06","3.07E-06","3.67E-06","1.00E-06",
            "1.61E-06","3.05E-08","3.73E-06","2.44E-06","6.03E-07","1.03E-06","1.29E-06","1.00E-06","7.35E-06",
            "1.53E-06","1.64E-06","6.41E-07","7.46E-07","2.99E-06","5.95E-07","1.06E-05","6.87E-07","5.74E-07",
            "3.73E-06","1.22E-06","5.89E-06","5.22E-06","2.55E-06","5.95E-07","4.43E-07","2.87E-06","1.68E-07",
            "2.47E-06","9.10E-06","9.62E-07","1.23E-06","9.77E-07","1.21E-06","3.67E-06","1.06E-06","4.19E-06",
            "1.61E-06","6.60E-07","8.04E-07","5.45E-07","2.04E-06", "NA", "NAN", "N/A"
        });

        mockData.put("ampca_bcm_2016_DAYS_TO_LAST_FOLLOWUP", new String[] {
            "3411","2798","822","523","1293","836","2695","1796","1734","979","1813","141","341","1021","55","832","579",
            "726","665","2746","1830","481","2380","1210","1921","765","1870","2159","1746","2164","537","1520","250",
            "1860","363","733","251","290","453","230","2074","762","192","1498","172","1100","1259","5768","18","842",
            "4810","3239","3634","2703","2191","305","253","1528","742","1260","1331","1148","307","1217","1155","994",
            "736","1056","952","531","636","901","557","898","240","377","802","919","821","835","556","526","261","562",
            "598","243","383","257","341","886","364","2441","1756","1058","215","2134","283","2978","3154","2992","494",
            "2968","2297","398","2211","2208","2065","2484","484","13","471","125","2296","477","838","460","457","18",
            "34","937","2267","755","3270","1267","179","1438","1899","59","1581","1323","1067","178","482","824","853",
            "876","663","708","711","556","638","1330","557","180","524","158","380","272","64","2555","10","374","699",
            "1446","578","97","233","879","448","651"
        });
        
        return mockData;
    }
    
    @Test
    public void testLinearDataBinner()
    {
        String studyId = "blca_tcga";
        String attributeId = "AGE";
        String[] values = mockData.get("blca_tcga_AGE");

        List<ClinicalData> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = clinicalData.stream().map(ClinicalData::getPatientId).collect(Collectors.toList());
        
        List<DataBin> dataBins = dataBinner.calculateClinicalDataBins(attributeId, clinicalData, patientIds);

        Assert.assertEquals(6, dataBins.size());
        
        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new Double(40.0), dataBins.get(0).getEnd());
        Assert.assertEquals(2, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new Double(40.0), dataBins.get(1).getStart());
        Assert.assertEquals(new Double(50.0), dataBins.get(1).getEnd());
        Assert.assertEquals(23, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new Double(50.0), dataBins.get(2).getStart());
        Assert.assertEquals(new Double(60.0), dataBins.get(2).getEnd());
        Assert.assertEquals(83, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new Double(60.0), dataBins.get(3).getStart());
        Assert.assertEquals(new Double(70.0), dataBins.get(3).getEnd());
        Assert.assertEquals(124, dataBins.get(3).getCount().intValue());

        Assert.assertEquals(new Double(70.0), dataBins.get(4).getStart());
        Assert.assertEquals(new Double(80.0), dataBins.get(4).getEnd());
        Assert.assertEquals(131, dataBins.get(4).getCount().intValue());

        Assert.assertEquals(new Double(80.0), dataBins.get(5).getStart());
        Assert.assertEquals(new Double(90.0), dataBins.get(5).getEnd());
        Assert.assertEquals(48, dataBins.get(5).getCount().intValue());
    }
    
    @Test
    public void testStaticDataBinnerFilter()
    {
        String studyId = "blca_tcga";
        String attributeId = "AGE";
        String[] values = mockData.get("blca_tcga_AGE");

        List<ClinicalData> unfilteredClinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> unfilteredPatientIds = 
            unfilteredClinicalData.stream().map(ClinicalData::getPatientId).collect(Collectors.toList());
        List<DataBin> unfilteredDataBins = dataBinner.calculateClinicalDataBins(
            attributeId, unfilteredClinicalData, unfilteredPatientIds);
        
        List<ClinicalData> filteredClinicalData = unfilteredClinicalData.subList(0, 108); // (0, 60] interval
        List<String> filteredPatientIds =
            filteredClinicalData.stream().map(ClinicalData::getPatientId).collect(Collectors.toList());
        List<DataBin> filteredDataBins = dataBinner.calculateClinicalDataBins(
            attributeId, filteredClinicalData, unfilteredClinicalData, filteredPatientIds, unfilteredPatientIds);
        
        // same number of bins for both
        Assert.assertEquals(6, unfilteredDataBins.size());
        Assert.assertEquals(6, filteredDataBins.size());

        // same start/end/special values for all bins
        
        Assert.assertEquals("<=", filteredDataBins.get(0).getSpecialValue());
        Assert.assertEquals("<=", unfilteredDataBins.get(0).getSpecialValue());
        Assert.assertEquals(new Double(40.0), filteredDataBins.get(0).getEnd());
        Assert.assertEquals(new Double(40.0), unfilteredDataBins.get(0).getEnd());

        Assert.assertEquals(new Double(40.0), filteredDataBins.get(1).getStart());
        Assert.assertEquals(new Double(40.0), unfilteredDataBins.get(1).getStart());
        Assert.assertEquals(new Double(50.0), filteredDataBins.get(1).getEnd());
        Assert.assertEquals(new Double(50.0), unfilteredDataBins.get(1).getEnd());

        Assert.assertEquals(new Double(50.0), filteredDataBins.get(2).getStart());
        Assert.assertEquals(new Double(50.0), unfilteredDataBins.get(2).getStart());
        Assert.assertEquals(new Double(60.0), filteredDataBins.get(2).getEnd());
        Assert.assertEquals(new Double(60.0), unfilteredDataBins.get(2).getEnd());

        Assert.assertEquals(new Double(60.0), filteredDataBins.get(3).getStart());
        Assert.assertEquals(new Double(60.0), unfilteredDataBins.get(3).getStart());
        Assert.assertEquals(new Double(70.0), filteredDataBins.get(3).getEnd());
        Assert.assertEquals(new Double(70.0), unfilteredDataBins.get(3).getEnd());

        Assert.assertEquals(new Double(70.0), filteredDataBins.get(4).getStart());
        Assert.assertEquals(new Double(70.0), unfilteredDataBins.get(4).getStart());
        Assert.assertEquals(new Double(80.0), filteredDataBins.get(4).getEnd());
        Assert.assertEquals(new Double(80.0), unfilteredDataBins.get(4).getEnd());

        Assert.assertEquals(new Double(80.0), filteredDataBins.get(5).getStart());
        Assert.assertEquals(new Double(80.0), unfilteredDataBins.get(5).getStart());
        Assert.assertEquals(new Double(90.0), filteredDataBins.get(5).getEnd());
        Assert.assertEquals(new Double(90.0), unfilteredDataBins.get(5).getEnd());
        
        // same counts until the bin (60-70]
        
        Assert.assertEquals(2, filteredDataBins.get(0).getCount().intValue());
        Assert.assertEquals(2, unfilteredDataBins.get(0).getCount().intValue());

        Assert.assertEquals(23, filteredDataBins.get(1).getCount().intValue());
        Assert.assertEquals(23, unfilteredDataBins.get(1).getCount().intValue());

        Assert.assertEquals(83, filteredDataBins.get(2).getCount().intValue());
        Assert.assertEquals(83, unfilteredDataBins.get(2).getCount().intValue());

        Assert.assertEquals(0, filteredDataBins.get(3).getCount().intValue());
        Assert.assertEquals(124, unfilteredDataBins.get(3).getCount().intValue());

        Assert.assertEquals(0, filteredDataBins.get(4).getCount().intValue());
        Assert.assertEquals(131, unfilteredDataBins.get(4).getCount().intValue());

        Assert.assertEquals(0, filteredDataBins.get(5).getCount().intValue());
        Assert.assertEquals(48, unfilteredDataBins.get(5).getCount().intValue());
    }

    @Test
    public void testLinearDataBinnerWithPediatricAge()
    {
        String studyId = "skcm_broad";
        String attributeId = "AGE_AT_PROCUREMENT";
        String[] values = mockData.get("skcm_broad_AGE_AT_PROCUREMENT");
        String[] patientsWithNoClinicalData = {
            "NA_PATIENT_01", "NA_PATIENT_02", "NA_PATIENT_03", "NA_PATIENT_04"
        };

        List<ClinicalData> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = clinicalData.stream().map(ClinicalData::getPatientId).collect(Collectors.toList());
        patientIds.addAll(Arrays.asList(patientsWithNoClinicalData));

        List<DataBin> dataBins = dataBinner.calculateClinicalDataBins(attributeId, clinicalData, patientIds);

        Assert.assertEquals(9, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new Double(18.0), dataBins.get(0).getEnd());
        Assert.assertEquals(1, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new Double(18.0), dataBins.get(1).getStart());
        Assert.assertEquals(new Double(30.0), dataBins.get(1).getEnd());
        Assert.assertEquals(9, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new Double(30.0), dataBins.get(2).getStart());
        Assert.assertEquals(new Double(40.0), dataBins.get(2).getEnd());
        Assert.assertEquals(16, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new Double(40.0), dataBins.get(3).getStart());
        Assert.assertEquals(new Double(50.0), dataBins.get(3).getEnd());
        Assert.assertEquals(31, dataBins.get(3).getCount().intValue());

        Assert.assertEquals(new Double(50.0), dataBins.get(4).getStart());
        Assert.assertEquals(new Double(60.0), dataBins.get(4).getEnd());
        Assert.assertEquals(25, dataBins.get(4).getCount().intValue());

        Assert.assertEquals(new Double(60.0), dataBins.get(5).getStart());
        Assert.assertEquals(new Double(70.0), dataBins.get(5).getEnd());
        Assert.assertEquals(24, dataBins.get(5).getCount().intValue());

        Assert.assertEquals(new Double(70.0), dataBins.get(6).getStart());
        Assert.assertEquals(new Double(80.0), dataBins.get(6).getEnd());
        Assert.assertEquals(11, dataBins.get(6).getCount().intValue());

        Assert.assertEquals(new Double(80.0), dataBins.get(7).getStart());
        Assert.assertEquals(">", dataBins.get(7).getSpecialValue());
        Assert.assertEquals(2, dataBins.get(7).getCount().intValue());
        
        Assert.assertEquals("NA", dataBins.get(8).getSpecialValue());
        Assert.assertEquals(4, dataBins.get(8).getCount().intValue());
    }

    @Test
    public void testLinearDataBinnerWithNA()
    {
        String studyId = "blca_tcga";
        String attributeId = "LYMPH_NODE_EXAMINED_COUNT";
        String[] values = mockData.get("blca_tcga_LYMPH_NODE_EXAMINED_COUNT");

        List<ClinicalData> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = clinicalData.stream().map(ClinicalData::getPatientId).collect(Collectors.toList());

        List<DataBin> dataBins = dataBinner.calculateClinicalDataBins(attributeId, clinicalData, patientIds);

        Assert.assertEquals(8, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new Double(10.0), dataBins.get(0).getEnd());
        Assert.assertEquals(71, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new Double(10.0), dataBins.get(1).getStart());
        Assert.assertEquals(new Double(20.0), dataBins.get(1).getEnd());
        Assert.assertEquals(94, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new Double(20.0), dataBins.get(2).getStart());
        Assert.assertEquals(new Double(30.0), dataBins.get(2).getEnd());
        Assert.assertEquals(58, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new Double(30.0), dataBins.get(3).getStart());
        Assert.assertEquals(new Double(40.0), dataBins.get(3).getEnd());
        Assert.assertEquals(19, dataBins.get(3).getCount().intValue());

        Assert.assertEquals(new Double(40.0), dataBins.get(4).getStart());
        Assert.assertEquals(new Double(50.0), dataBins.get(4).getEnd());
        Assert.assertEquals(17, dataBins.get(4).getCount().intValue());

        Assert.assertEquals(new Double(50.0), dataBins.get(5).getStart());
        Assert.assertEquals(new Double(60.0), dataBins.get(5).getEnd());
        Assert.assertEquals(10, dataBins.get(5).getCount().intValue());

        Assert.assertEquals(new Double(60.0), dataBins.get(6).getStart());
        Assert.assertEquals(">", dataBins.get(6).getSpecialValue());
        Assert.assertEquals(34, dataBins.get(6).getCount().intValue());
        
        Assert.assertEquals("NA", dataBins.get(7).getSpecialValue());
        Assert.assertEquals(109, dataBins.get(7).getCount().intValue());
    }    
    
    @Test
    public void testDiscreteDataBinner()
    {
        String studyId = "acyc_fmi_2014";
        String attributeId = "ACTIONABLE_ALTERATIONS";
        String[] values = mockData.get("acyc_fmi_2014_ACTIONABLE_ALTERATIONS");

        List<ClinicalData> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = clinicalData.stream().map(ClinicalData::getPatientId).collect(Collectors.toList());

        List<DataBin> dataBins = dataBinner.calculateClinicalDataBins(attributeId, clinicalData, patientIds);

        Assert.assertEquals(5, dataBins.size());
        
        Assert.assertEquals(new Double(0.0), dataBins.get(0).getStart());
        Assert.assertEquals(new Double(0.0), dataBins.get(0).getEnd());
        Assert.assertEquals(16, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new Double(1.0), dataBins.get(1).getStart());
        Assert.assertEquals(new Double(1.0), dataBins.get(1).getEnd());
        Assert.assertEquals(6, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new Double(2.0), dataBins.get(2).getStart());
        Assert.assertEquals(new Double(2.0), dataBins.get(2).getEnd());
        Assert.assertEquals(4, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new Double(3.0), dataBins.get(3).getStart());
        Assert.assertEquals(new Double(3.0), dataBins.get(3).getEnd());
        Assert.assertEquals(1, dataBins.get(3).getCount().intValue());

        Assert.assertEquals(new Double(5.0), dataBins.get(4).getStart());
        Assert.assertEquals(new Double(5.0), dataBins.get(4).getEnd());
        Assert.assertEquals(1, dataBins.get(4).getCount().intValue());
    }

    @Test
    public void testScientificDataBinner()
    {
        String studyId = "blca_dfarber_mskcc_2014";
        String attributeId = "SILENT_RATE";
        String[] values = mockData.get("blca_dfarber_mskcc_2014_SILENT_RATE");
        String[] samplesWithNoClinicalData = {
            "NA_SAMPLE_01", "NA_SAMPLE_02", "NA_SAMPLE_03", "NA_SAMPLE_04", "NA_SAMPLE_05", "NA_SAMPLE_06"
        };

        List<ClinicalData> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> sampleIds = clinicalData.stream().map(ClinicalData::getSampleId).collect(Collectors.toList());
        sampleIds.addAll(Arrays.asList(samplesWithNoClinicalData));

        List<DataBin> dataBins = dataBinner.calculateClinicalDataBins(attributeId, clinicalData, sampleIds);

        Assert.assertEquals(5, dataBins.size());

        Assert.assertEquals(new Double("1e-8"), dataBins.get(0).getStart());
        Assert.assertEquals(new Double("1e-7"), dataBins.get(0).getEnd());
        Assert.assertEquals(1, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new Double("1e-7"), dataBins.get(1).getStart());
        Assert.assertEquals(new Double("1e-6"), dataBins.get(1).getEnd());
        Assert.assertEquals(16, dataBins.get(1).getCount().intValue());

        Assert.assertEquals(new Double("1e-6"), dataBins.get(2).getStart());
        Assert.assertEquals(new Double("1e-5"), dataBins.get(2).getEnd());
        Assert.assertEquals(32, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(">", dataBins.get(3).getSpecialValue());
        Assert.assertEquals(new Double("1e-5"), dataBins.get(3).getStart());
        Assert.assertEquals(1, dataBins.get(3).getCount().intValue());

        Assert.assertEquals("NA", dataBins.get(4).getSpecialValue());
        Assert.assertEquals(3 + 6, dataBins.get(4).getCount().intValue());
    }
    
    @Test
    public void testLogScaleDataBinner()
    {
        String studyId = "ampca_bcm_2016";
        String attributeId = "DAYS_TO_LAST_FOLLOWUP";
        String[] values = mockData.get("ampca_bcm_2016_DAYS_TO_LAST_FOLLOWUP");

        List<ClinicalData> clinicalData = mockClinicalData(attributeId, studyId, values);
        List<String> patientIds = clinicalData.stream().map(ClinicalData::getPatientId).collect(Collectors.toList());

        List<DataBin> dataBins = dataBinner.calculateClinicalDataBins(attributeId, clinicalData, patientIds);

        Assert.assertEquals(7, dataBins.size());

        Assert.assertEquals("<=", dataBins.get(0).getSpecialValue());
        Assert.assertEquals(new Double(10.0), dataBins.get(0).getEnd());
        Assert.assertEquals(1, dataBins.get(0).getCount().intValue());

        Assert.assertEquals(new Double(10.0), dataBins.get(1).getStart());
        Assert.assertEquals(new Double(31.0), dataBins.get(1).getEnd());
        Assert.assertEquals(3, dataBins.get(1).getCount().intValue());
        
        Assert.assertEquals(new Double(31.0), dataBins.get(2).getStart());
        Assert.assertEquals(new Double(100.0), dataBins.get(2).getEnd());
        Assert.assertEquals(5, dataBins.get(2).getCount().intValue());

        Assert.assertEquals(new Double(100.0), dataBins.get(3).getStart());
        Assert.assertEquals(new Double(316.0), dataBins.get(3).getEnd());
        Assert.assertEquals(23, dataBins.get(3).getCount().intValue());

        Assert.assertEquals(new Double(316.0), dataBins.get(4).getStart());
        Assert.assertEquals(new Double(1000.0), dataBins.get(4).getEnd());
        Assert.assertEquals(67, dataBins.get(4).getCount().intValue());

        Assert.assertEquals(new Double(1000.0), dataBins.get(5).getStart());
        Assert.assertEquals(new Double(3162.0), dataBins.get(5).getEnd());
        Assert.assertEquals(55, dataBins.get(5).getCount().intValue());

        Assert.assertEquals(new Double(3162.0), dataBins.get(6).getStart());
        Assert.assertEquals(new Double(10000.0), dataBins.get(6).getEnd());
        Assert.assertEquals(6, dataBins.get(6).getCount().intValue());
    }
    
    private List<ClinicalData> mockClinicalData(String attributeId, String studyId, String[] values)
    {
        List<ClinicalData> clinicalDataList =  new ArrayList<>();
        
        for (int index = 0; index < values.length; index++) 
        {
            ClinicalData clinicalData = new ClinicalData();
            
            clinicalData.setAttrId(attributeId);
            clinicalData.setStudyId(studyId);
            clinicalData.setSampleId(studyId + "_sample_" + index);
            clinicalData.setPatientId(studyId + "_patient_" + index);
            clinicalData.setAttrValue(values[index]);
            
            clinicalDataList.add(clinicalData);
        }
        
        return clinicalDataList;
    }
}
