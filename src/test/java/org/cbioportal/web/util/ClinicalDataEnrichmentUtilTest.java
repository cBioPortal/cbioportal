package org.cbioportal.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.ClinicalDataEnrichment;
import org.cbioportal.model.Sample;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.util.ClinicalAttributeUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClinicalDataEnrichmentUtilTest {

    public static final String STUDY_ID1 = "study_id_1";
    public static final String STUDY_ID2 = "study_id_2";
    public static final String SAMPLE_ID1 = "sample_id1";
    public static final String SAMPLE_ID2 = "sample_id2";
    public static final String SAMPLE_ID3 = "sample_id3";
    public static final String SAMPLE_ID4 = "sample_id4";
    public static final String SAMPLE_ID5 = "sample_id5";

    public static final String PATIENT_ID1 = "patient_id1";
    public static final String PATIENT_ID2 = "patient_id2";
    public static final String PATIENT_ID3 = "patient_id3";
    public static final String PATIENT_ID4 = "patient_id4";
    public static final String PATIENT_ID5 = "patient_id5";
    public static final String CLINICAL_ATTRIBUTE_ID_1 = "attribute_id1";
    public static final String CLINICAL_ATTRIBUTE_ID_2 = "attribute_id2";
    public static final String CLINICAL_ATTRIBUTE_ID_3 = "attribute_id3";
    public static final String CLINICAL_ATTRIBUTE_ID_4 = "attribute_id4";

    @InjectMocks
    private ClinicalDataEnrichmentUtil clinicalDataEnrichmentUtil;

    @Mock
    private ClinicalDataService clinicalDataService;

    @Mock
    private ClinicalAttributeService clinicalAttributeService;

    @Mock
    private SampleService sampleService;
    
    @Spy
    private ClinicalAttributeUtil clinicalAttributeUtil = new ClinicalAttributeUtil();

    @Test
    public void fetchClinicalDataEnrichemnts() {

        List<ClinicalData> expectedSampleClinicalDataList = new ArrayList<>();
        ClinicalData sampleClinicalData = new ClinicalData();
        expectedSampleClinicalDataList.add(sampleClinicalData);

        Sample sample1 = new Sample();
        sample1.setStableId(SAMPLE_ID1);
        sample1.setPatientStableId(PATIENT_ID1);
        sample1.setCancerStudyIdentifier(STUDY_ID1);

        Sample sample2 = new Sample();
        sample2.setStableId(SAMPLE_ID2);
        sample2.setPatientStableId(PATIENT_ID2);
        sample2.setCancerStudyIdentifier(STUDY_ID1);

        Sample sample3 = new Sample();
        sample3.setStableId(SAMPLE_ID3);
        sample3.setPatientStableId(PATIENT_ID3);
        sample3.setCancerStudyIdentifier(STUDY_ID2);

        Sample sample4 = new Sample();
        sample4.setStableId(SAMPLE_ID4);
        sample4.setPatientStableId(PATIENT_ID4);
        sample4.setCancerStudyIdentifier(STUDY_ID2);

        Sample sample5 = new Sample();
        sample5.setStableId(SAMPLE_ID5);
        sample5.setPatientStableId(PATIENT_ID5);
        sample5.setCancerStudyIdentifier(STUDY_ID2);

        ClinicalAttribute attribute1 = new ClinicalAttribute();
        attribute1.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        attribute1.setDatatype("STRING");
        attribute1.setPatientAttribute(false);

        ClinicalAttribute attribute2 = new ClinicalAttribute();
        attribute2.setAttrId(CLINICAL_ATTRIBUTE_ID_2);
        attribute2.setDatatype("STRING");
        attribute2.setPatientAttribute(true);

        ClinicalAttribute attribute3 = new ClinicalAttribute();
        attribute3.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        attribute3.setDatatype("NUMBER");
        attribute3.setPatientAttribute(true);

        ClinicalAttribute attribute4 = new ClinicalAttribute();
        attribute4.setAttrId(CLINICAL_ATTRIBUTE_ID_4);
        attribute4.setDatatype("NUMBER");
        attribute4.setPatientAttribute(true);

        List<ClinicalAttribute> attributes = Arrays.asList(attribute1, attribute2, attribute3, attribute4);

        List<List<Sample>> groupedSamples = new ArrayList<List<Sample>>();
        groupedSamples.add(Arrays.asList(sample1, sample2));
        groupedSamples.add(Arrays.asList(sample3, sample4, sample5));

        ClinicalDataCount sampleClinicalDataCount1 = new ClinicalDataCount();
        sampleClinicalDataCount1.setAttributeId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalDataCount1.setValue("TEST_CLINICAL_DATA_VALUE_1");
        sampleClinicalDataCount1.setCount(1);

        ClinicalDataCount sampleClinicalDataCount2 = new ClinicalDataCount();
        sampleClinicalDataCount2.setAttributeId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalDataCount2.setValue("TEST_CLINICAL_DATA_VALUE_2");
        sampleClinicalDataCount2.setCount(1);

        ClinicalDataCount patientClinicalDataCount1 = new ClinicalDataCount();
        patientClinicalDataCount1.setAttributeId(CLINICAL_ATTRIBUTE_ID_2);
        patientClinicalDataCount1.setValue("TEST_CLINICAL_DATA_VALUE_1");
        patientClinicalDataCount1.setCount(1);

        ClinicalDataCount patientClinicalDataCount2 = new ClinicalDataCount();
        patientClinicalDataCount2.setAttributeId(CLINICAL_ATTRIBUTE_ID_2);
        patientClinicalDataCount2.setValue("TEST_CLINICAL_DATA_VALUE_2");
        patientClinicalDataCount2.setCount(1);

        ClinicalDataCountItem group1sampleClinicalDataCountItem = new ClinicalDataCountItem();
        group1sampleClinicalDataCountItem.setAttributeId(CLINICAL_ATTRIBUTE_ID_1);
        group1sampleClinicalDataCountItem.setCounts(Arrays.asList(sampleClinicalDataCount1, sampleClinicalDataCount2));

        ClinicalDataCountItem group1patientClinicalDataCountItem = new ClinicalDataCountItem();
        group1patientClinicalDataCountItem.setAttributeId(CLINICAL_ATTRIBUTE_ID_2);
        group1patientClinicalDataCountItem
                .setCounts(Arrays.asList(patientClinicalDataCount1, patientClinicalDataCount2));

        when(clinicalDataService.fetchClinicalDataCounts(anyList(),
                anyList(), anyList()))
                .thenReturn(new ArrayList<ClinicalDataCountItem>());

        // when there is no data
        Assert.assertTrue(
                clinicalDataEnrichmentUtil.createEnrichmentsForCategoricalData(attributes, groupedSamples).isEmpty());


        when(clinicalDataService.fetchClinicalDataCounts(Arrays.asList(STUDY_ID1, STUDY_ID1),
                Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), Arrays.asList(CLINICAL_ATTRIBUTE_ID_1, CLINICAL_ATTRIBUTE_ID_2))).thenReturn(Arrays.asList(group1sampleClinicalDataCountItem, group1patientClinicalDataCountItem));

        // where there are no attributes with STRING datatype
        Assert.assertTrue(clinicalDataEnrichmentUtil
                .createEnrichmentsForCategoricalData(Arrays.asList(attribute3, attribute4), groupedSamples).isEmpty());

        // when attributes is empty
        Assert.assertTrue(clinicalDataEnrichmentUtil
                .createEnrichmentsForCategoricalData(new ArrayList<ClinicalAttribute>(), groupedSamples).isEmpty());

        List<ClinicalDataEnrichment> actualClinicalDataEnrichments = clinicalDataEnrichmentUtil
                .createEnrichmentsForCategoricalData(attributes, groupedSamples);

        // when there is data for only one group
        Assert.assertEquals(0, actualClinicalDataEnrichments.size());

        ClinicalDataCount sampleClinicalDataCount3 = new ClinicalDataCount();
        sampleClinicalDataCount3.setAttributeId(CLINICAL_ATTRIBUTE_ID_1);
        sampleClinicalDataCount3.setValue("TEST_CLINICAL_DATA_VALUE_3");
        sampleClinicalDataCount3.setCount(3);

        ClinicalDataCount patientClinicalDataCount3 = new ClinicalDataCount();
        patientClinicalDataCount3.setAttributeId(CLINICAL_ATTRIBUTE_ID_2);
        patientClinicalDataCount3.setValue("TEST_CLINICAL_DATA_VALUE_3");
        patientClinicalDataCount3.setCount(3);

        ClinicalDataCountItem group2sampleClinicalDataCountItem = new ClinicalDataCountItem();
        group2sampleClinicalDataCountItem.setAttributeId(CLINICAL_ATTRIBUTE_ID_1);
        group2sampleClinicalDataCountItem.setCounts(Arrays.asList(sampleClinicalDataCount3));

        ClinicalDataCountItem group2patientClinicalDataCountItem = new ClinicalDataCountItem();
        group2patientClinicalDataCountItem.setAttributeId(CLINICAL_ATTRIBUTE_ID_2);
        group2patientClinicalDataCountItem.setCounts(Arrays.asList(patientClinicalDataCount3));

        // data only for string datatype and for all groups
        when(clinicalDataService.fetchClinicalDataCounts(Arrays.asList(STUDY_ID2, STUDY_ID2, STUDY_ID2),
                Arrays.asList(SAMPLE_ID3, SAMPLE_ID4, SAMPLE_ID5), Arrays.asList(CLINICAL_ATTRIBUTE_ID_1,CLINICAL_ATTRIBUTE_ID_2))).thenReturn(Arrays.asList(group2sampleClinicalDataCountItem, group2patientClinicalDataCountItem));

        actualClinicalDataEnrichments = clinicalDataEnrichmentUtil.createEnrichmentsForCategoricalData(attributes,
                groupedSamples);

        // when there is data for more than one group
        Assert.assertEquals(2, actualClinicalDataEnrichments.size());
        Assert.assertEquals("0.08208499862670093", actualClinicalDataEnrichments.get(0).getpValue().toString());
        Assert.assertEquals("4.999999999999999", actualClinicalDataEnrichments.get(0).getScore().toString());
        Assert.assertEquals("Chi-squared Test", actualClinicalDataEnrichments.get(0).getMethod());

    }

    @Test
    public void createEnrichmentsForNumericData() {

        List<ClinicalData> expectedSampleClinicalDataList = new ArrayList<>();
        ClinicalData sampleClinicalData = new ClinicalData();
        expectedSampleClinicalDataList.add(sampleClinicalData);

        Sample sample1 = new Sample();
        sample1.setStableId(SAMPLE_ID1);
        sample1.setPatientStableId(PATIENT_ID1);
        sample1.setCancerStudyIdentifier(STUDY_ID1);

        Sample sample2 = new Sample();
        sample2.setStableId(SAMPLE_ID2);
        sample2.setPatientStableId(PATIENT_ID2);
        sample2.setCancerStudyIdentifier(STUDY_ID1);

        Sample sample3 = new Sample();
        sample3.setStableId(SAMPLE_ID3);
        sample3.setPatientStableId(PATIENT_ID3);
        sample3.setCancerStudyIdentifier(STUDY_ID2);

        Sample sample4 = new Sample();
        sample4.setStableId(SAMPLE_ID4);
        sample4.setPatientStableId(PATIENT_ID4);
        sample4.setCancerStudyIdentifier(STUDY_ID2);

        Sample sample5 = new Sample();
        sample5.setStableId(SAMPLE_ID5);
        sample5.setPatientStableId(PATIENT_ID5);
        sample5.setCancerStudyIdentifier(STUDY_ID2);

        ClinicalAttribute attribute1 = new ClinicalAttribute();
        attribute1.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        attribute1.setDatatype("STRING");
        attribute1.setPatientAttribute(false);

        ClinicalAttribute attribute2 = new ClinicalAttribute();
        attribute2.setAttrId(CLINICAL_ATTRIBUTE_ID_2);
        attribute2.setDatatype("STRING");
        attribute2.setPatientAttribute(true);

        ClinicalAttribute attribute3 = new ClinicalAttribute();
        attribute3.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        attribute3.setDatatype("NUMBER");
        attribute3.setPatientAttribute(false);

        ClinicalAttribute attribute4 = new ClinicalAttribute();
        attribute4.setAttrId(CLINICAL_ATTRIBUTE_ID_4);
        attribute4.setDatatype("NUMBER");
        attribute4.setPatientAttribute(true);

        List<ClinicalAttribute> attributes = Arrays.asList(attribute1, attribute2, attribute3, attribute4);

        List<List<Sample>> groupedSamples = new ArrayList<List<Sample>>();
        groupedSamples.add(Arrays.asList(sample1, sample2));
        groupedSamples.add(Arrays.asList(sample3, sample4, sample5));

        ClinicalData sampleClinicalData1 = new ClinicalData();
        sampleClinicalData1.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        sampleClinicalData1.setAttrValue("2");
        sampleClinicalData1.setSampleId(SAMPLE_ID1);
        sampleClinicalData1.setStudyId(STUDY_ID1);

        ClinicalData sampleClinicalData2 = new ClinicalData();
        sampleClinicalData2.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        sampleClinicalData2.setAttrValue("3");
        sampleClinicalData2.setSampleId(SAMPLE_ID2);
        sampleClinicalData2.setStudyId(STUDY_ID1);

        ClinicalData patientClinicalData1 = new ClinicalData();
        patientClinicalData1.setAttrId(CLINICAL_ATTRIBUTE_ID_4);
        patientClinicalData1.setAttrValue("2");
        patientClinicalData1.setSampleId(SAMPLE_ID1);
        patientClinicalData1.setStudyId(STUDY_ID1);

        ClinicalData patientClinicalData2 = new ClinicalData();
        patientClinicalData2.setAttrId(CLINICAL_ATTRIBUTE_ID_4);
        patientClinicalData2.setAttrValue("1");
        patientClinicalData2.setSampleId(SAMPLE_ID1);
        patientClinicalData2.setStudyId(STUDY_ID1);

        when(clinicalDataService.fetchClinicalData(anyList(), anyList(), anyList(), anyString(), anyString()))
                .thenReturn(new ArrayList<ClinicalData>());

        // when no data
        Assert.assertTrue(
            clinicalDataEnrichmentUtil.createEnrichmentsForNumericData(attributes, groupedSamples).isEmpty()
        );

        // data only for one group
        when(clinicalDataService.fetchClinicalData(Arrays.asList(STUDY_ID1, STUDY_ID1),
                Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), Arrays.asList(CLINICAL_ATTRIBUTE_ID_3), "SAMPLE", "SUMMARY"))
                .thenReturn(Arrays.asList(sampleClinicalData1, sampleClinicalData2));

        when(clinicalDataService.fetchClinicalData(Arrays.asList(STUDY_ID1, STUDY_ID1),
                Arrays.asList(PATIENT_ID1, PATIENT_ID2), Arrays.asList(CLINICAL_ATTRIBUTE_ID_4), "PATIENT", "SUMMARY"))
                .thenReturn(Arrays.asList(patientClinicalData1, patientClinicalData2));

        // when there is data only for one group
        Assert.assertTrue(
                clinicalDataEnrichmentUtil.createEnrichmentsForNumericData(attributes, groupedSamples).isEmpty());

        ClinicalData sampleClinicalData3 = new ClinicalData();
        sampleClinicalData3.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        sampleClinicalData3.setAttrValue("1");
        sampleClinicalData3.setSampleId(SAMPLE_ID3);
        sampleClinicalData3.setStudyId(STUDY_ID2);

        ClinicalData sampleClinicalData4 = new ClinicalData();
        sampleClinicalData4.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        sampleClinicalData4.setAttrValue("5");
        sampleClinicalData4.setSampleId(SAMPLE_ID4);
        sampleClinicalData4.setStudyId(STUDY_ID2);

        ClinicalData sampleClinicalData5 = new ClinicalData();
        sampleClinicalData5.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        sampleClinicalData5.setAttrValue("2");
        sampleClinicalData5.setSampleId(SAMPLE_ID5);
        sampleClinicalData5.setStudyId(STUDY_ID2);

        ClinicalData patientClinicalData3 = new ClinicalData();
        patientClinicalData3.setAttrId(CLINICAL_ATTRIBUTE_ID_4);
        patientClinicalData3.setAttrValue("5");
        patientClinicalData3.setSampleId(SAMPLE_ID3);
        patientClinicalData3.setStudyId(STUDY_ID2);

        ClinicalData patientClinicalData4 = new ClinicalData();
        patientClinicalData4.setAttrId(CLINICAL_ATTRIBUTE_ID_4);
        patientClinicalData4.setAttrValue("3");
        patientClinicalData4.setSampleId(SAMPLE_ID4);
        patientClinicalData4.setStudyId(STUDY_ID2);

        ClinicalData patientClinicalData5 = new ClinicalData();
        patientClinicalData5.setAttrId(CLINICAL_ATTRIBUTE_ID_4);
        patientClinicalData5.setAttrValue("1");
        patientClinicalData5.setSampleId(SAMPLE_ID5);
        patientClinicalData5.setStudyId(STUDY_ID2);

        when(clinicalDataService.fetchClinicalData(Arrays.asList(STUDY_ID2, STUDY_ID2, STUDY_ID2),
                Arrays.asList(SAMPLE_ID3, SAMPLE_ID4, SAMPLE_ID5), Arrays.asList(CLINICAL_ATTRIBUTE_ID_3), "SAMPLE",
                "SUMMARY")).thenReturn(Arrays.asList(sampleClinicalData3, sampleClinicalData4, sampleClinicalData5));

        when(clinicalDataService.fetchClinicalData(Arrays.asList(STUDY_ID2, STUDY_ID2, STUDY_ID2),
                Arrays.asList(PATIENT_ID3, PATIENT_ID4, PATIENT_ID5), Arrays.asList(CLINICAL_ATTRIBUTE_ID_4), "PATIENT",
                "SUMMARY")).thenReturn(Arrays.asList(patientClinicalData3, patientClinicalData4, patientClinicalData5));

        List<ClinicalDataEnrichment> actualClinicalDataEnrichments = clinicalDataEnrichmentUtil
                .createEnrichmentsForNumericData(attributes, groupedSamples);

        // when there is data for more than one group
        Assert.assertEquals(2, actualClinicalDataEnrichments.size());
        Assert.assertEquals("0.7670968826920188", actualClinicalDataEnrichments.get(0).getpValue().toString());
        Assert.assertEquals("0.08771942638231253", actualClinicalDataEnrichments.get(0).getScore().toString());
        Assert.assertEquals("Wilcoxon Test", actualClinicalDataEnrichments.get(0).getMethod());
    }
}
