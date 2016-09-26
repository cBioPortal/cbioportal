package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class ClinicalDataMyBatisRepositoryTest {

    @Autowired
    private ClinicalDataMyBatisRepository clinicalDataMyBatisRepository;

    @Test
    public void getAllClinicalDataOfSampleInStudySingleStudyNullSampleNullAttributeIdProjection() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<SampleClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy(studyIds, null, null, "ID", null, null,
                        null, null);

        Assert.assertEquals(6, result.size());
        SampleClinicalData data = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", data.getAttrId());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
        Assert.assertNull(data.getSample());
        Assert.assertNull(data.getAttrValue());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudyEmptyResult() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("invalid_study");
        List<SampleClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy(studyIds, null, null, "ID", null, null,
                        null, null);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudySingleStudyNullSampleNullAttributeSummaryProjection()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<SampleClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy(studyIds, null, null, "SUMMARY", null,
                        null, null, null);

        Assert.assertEquals(6, result.size());
        SampleClinicalData data = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", data.getAttrId());
        Assert.assertEquals("276", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
        Assert.assertNull(data.getSample());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudySingleStudyNullSampleNullAttributeDetailedProjection()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<SampleClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy(studyIds, null, null, "DETAILED", null,
                        null, null, null);

        Assert.assertEquals(6, result.size());
        SampleClinicalData data = result.get(0);
        Assert.assertEquals("OTHER_SAMPLE_ID", data.getAttrId());
        Assert.assertEquals("5C631CE8-F96A-4C35-A459-556FC4AB21E1", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Sample sample = data.getSample();
        Assert.assertEquals((Integer) 1, sample.getInternalId());
        Assert.assertEquals((Integer) 1, sample.getPatientId());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, sample.getSampleType());
        Assert.assertEquals("TCGA-A1-A0SB-01", sample.getStableId());
        Assert.assertEquals("brca", sample.getTypeOfCancerId());
        Patient patient = sample.getPatient();
        Assert.assertEquals((Integer) 1, patient.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB", patient.getStableId());
        Assert.assertEquals((Integer) 1, patient.getCancerStudyId());
        CancerStudy cancerStudy = patient.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("TCGA, Nature 2012", cancerStudy.getCitation());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast " +
                "Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci.nih." +
                "gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("23000897", cancerStudy.getPmid());
        TypeOfCancer typeOfCancer = cancerStudy.getTypeOfCancer();
        Assert.assertEquals("brca", typeOfCancer.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma", typeOfCancer.getName());
        Assert.assertEquals("breast,breast invasive", typeOfCancer.getClinicalTrialKeywords());
        Assert.assertEquals("HotPink", typeOfCancer.getDedicatedColor());
        Assert.assertEquals("Breast", typeOfCancer.getShortName());
        Assert.assertEquals("tissue", typeOfCancer.getParent());
        ClinicalAttribute clinicalAttribute = data.getClinicalAttribute();
        Assert.assertEquals("OTHER_SAMPLE_ID", clinicalAttribute.getAttrId());
        Assert.assertEquals("Legacy DMP sample identifier (DMPnnnn)", clinicalAttribute.getDescription());
        Assert.assertEquals("STRING", clinicalAttribute.getDatatype());
        Assert.assertEquals("Other Sample ID", clinicalAttribute.getDisplayName());
        Assert.assertEquals(false, clinicalAttribute.getPatientAttribute());
        Assert.assertEquals("1", clinicalAttribute.getPriority());
        cancerStudy = clinicalAttribute.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("TCGA, Nature 2012", cancerStudy.getCitation());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast " +
                "Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci.nih." +
                "gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("23000897", cancerStudy.getPmid());
        typeOfCancer = cancerStudy.getTypeOfCancer();
        Assert.assertEquals("brca", typeOfCancer.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma", typeOfCancer.getName());
        Assert.assertEquals("breast,breast invasive", typeOfCancer.getClinicalTrialKeywords());
        Assert.assertEquals("HotPink", typeOfCancer.getDedicatedColor());
        Assert.assertEquals("Breast", typeOfCancer.getShortName());
        Assert.assertEquals("tissue", typeOfCancer.getParent());

    }

    @Test
    public void getAllClinicalDataOfSampleInStudySingleStudySingleSampleNullAttributeSummaryProjection()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        List<SampleClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy(studyIds, sampleIds, null, "SUMMARY",
                        null, null, null, null);

        Assert.assertEquals(3, result.size());
        SampleClinicalData data = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", data.getAttrId());
        Assert.assertEquals("276", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
        Assert.assertNull(data.getSample());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudySingleStudySingleSampleWithAttributeSummaryProjection()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        List<SampleClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy(studyIds, sampleIds, "OTHER_SAMPLE_ID",
                        "SUMMARY", null, null, null, null);

        Assert.assertEquals(1, result.size());
        SampleClinicalData data = result.get(0);
        Assert.assertEquals("OTHER_SAMPLE_ID", data.getAttrId());
        Assert.assertEquals("5C631CE8-F96A-4C35-A459-556FC4AB21E1", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
        Assert.assertNull(data.getSample());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudySingleStudyMultipleSampleNullAttributeSummaryProjection()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-A0SD-01");
        List<SampleClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy(studyIds, sampleIds, null, "SUMMARY",
                        null, null, null, null);

        Assert.assertEquals(6, result.size());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudyMultipleStudyMultipleSampleNullAttributeSummaryProjection()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("acc_tcga");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-B0SO-01");
        List<SampleClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy(studyIds, sampleIds, null, "SUMMARY",
                        null, null, null, null);

        Assert.assertEquals(5, result.size());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudySingleStudySingleSampleNullAttributeSummaryProjection2PageSize()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        List<SampleClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy(studyIds, sampleIds, null, "SUMMARY",
                        2, 0, null, null);

        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudySingleStudySingleSampleNullAttributeSummaryProjectionAttrIdSort()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        List<SampleClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy(studyIds, sampleIds, null, "SUMMARY",
                        null, null, "attrId", "ASC");

        Assert.assertEquals(3, result.size());
        Assert.assertEquals("DAYS_TO_COLLECTION", result.get(0).getAttrId());
        Assert.assertEquals("IS_FFPE", result.get(1).getAttrId());
        Assert.assertEquals("OTHER_SAMPLE_ID", result.get(2).getAttrId());
    }


    @Test
    public void getMetaSampleClinicalDataSingleStudyNullSampleNullAttribute() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        BaseMeta result = clinicalDataMyBatisRepository.getMetaSampleClinicalData(studyIds, null, null);
        Assert.assertEquals((Integer) 6, result.getTotalCount());
    }

    @Test
    public void getMetaSampleClinicalDataZeroCount() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("invalid_study");
        BaseMeta result = clinicalDataMyBatisRepository.getMetaSampleClinicalData(studyIds, null, null);
        Assert.assertEquals((Integer) 0, result.getTotalCount());
    }

    @Test
    public void getMetaSampleClinicalDataSingleStudySingleSampleNullAttribute() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        BaseMeta result = clinicalDataMyBatisRepository.getMetaSampleClinicalData(studyIds, sampleIds, null);
        Assert.assertEquals((Integer) 3, result.getTotalCount());
    }

    @Test
    public void getMetaSampleClinicalDataSingleStudySingleSampleWithAttribute() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        BaseMeta result = clinicalDataMyBatisRepository.getMetaSampleClinicalData(studyIds, sampleIds,
                "OTHER_SAMPLE_ID");

        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void getMetaSampleClinicalDataMultipleStudyMultipleSampleNullAttribute() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("acc_tcga");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-B0SO-01");
        BaseMeta result = clinicalDataMyBatisRepository.getMetaSampleClinicalData(studyIds, sampleIds, null);

        Assert.assertEquals((Integer) 5, result.getTotalCount());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudySingleStudyNullPatientNullAttributeIdProjection() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<PatientClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy(studyIds, null, null, "ID", null, null,
                        null, null);

        Assert.assertEquals(4, result.size());
        PatientClinicalData data = result.get(0);
        Assert.assertEquals("FORM_COMPLETION_DATE", data.getAttrId());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
        Assert.assertNull(data.getPatient());
        Assert.assertNull(data.getAttrValue());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudyEmptyResult() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("invalid_study");
        List<PatientClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy(studyIds, null, null, "ID", null, null,
                        null, null);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudySingleStudyNullPatientNullAttributeSummaryProjection()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<PatientClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy(studyIds, null, null, "SUMMARY", null,
                        null, null, null);

        Assert.assertEquals(4, result.size());
        PatientClinicalData data = result.get(0);
        Assert.assertEquals("FORM_COMPLETION_DATE", data.getAttrId());
        Assert.assertEquals("2013-12-5", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
        Assert.assertNull(data.getPatient());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudySingleStudyNullPatientNullAttributeDetailedProjection()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<PatientClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy(studyIds, null, null, "DETAILED", null,
                        null, null, null);

        Assert.assertEquals(4, result.size());
        PatientClinicalData data = result.get(0);
        Assert.assertEquals("RETROSPECTIVE_COLLECTION", data.getAttrId());
        Assert.assertEquals("NO", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Patient patient = data.getPatient();
        Assert.assertEquals((Integer) 1, patient.getInternalId());
        Assert.assertEquals("TCGA-A1-A0SB", patient.getStableId());
        Assert.assertEquals((Integer) 1, patient.getCancerStudyId());
        CancerStudy cancerStudy = patient.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("TCGA, Nature 2012", cancerStudy.getCitation());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast " +
                "Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci.nih." +
                "gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("23000897", cancerStudy.getPmid());
        TypeOfCancer typeOfCancer = cancerStudy.getTypeOfCancer();
        Assert.assertEquals("brca", typeOfCancer.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma", typeOfCancer.getName());
        Assert.assertEquals("breast,breast invasive", typeOfCancer.getClinicalTrialKeywords());
        Assert.assertEquals("HotPink", typeOfCancer.getDedicatedColor());
        Assert.assertEquals("Breast", typeOfCancer.getShortName());
        Assert.assertEquals("tissue", typeOfCancer.getParent());
        ClinicalAttribute clinicalAttribute = data.getClinicalAttribute();
        Assert.assertEquals("RETROSPECTIVE_COLLECTION", clinicalAttribute.getAttrId());
        Assert.assertEquals("Text indicator for the time frame of tissue procurement, indicating that the tissue was " +
                "obtained and stored prior to the initiation of the project.", clinicalAttribute.getDescription());
        Assert.assertEquals("STRING", clinicalAttribute.getDatatype());
        Assert.assertEquals("Tissue Retrospective Collection Indicator", clinicalAttribute.getDisplayName());
        Assert.assertEquals(true, clinicalAttribute.getPatientAttribute());
        Assert.assertEquals("1", clinicalAttribute.getPriority());
        cancerStudy = clinicalAttribute.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("TCGA, Nature 2012", cancerStudy.getCitation());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast " +
                "Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci.nih." +
                "gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("23000897", cancerStudy.getPmid());
        typeOfCancer = cancerStudy.getTypeOfCancer();
        Assert.assertEquals("brca", typeOfCancer.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma", typeOfCancer.getName());
        Assert.assertEquals("breast,breast invasive", typeOfCancer.getClinicalTrialKeywords());
        Assert.assertEquals("HotPink", typeOfCancer.getDedicatedColor());
        Assert.assertEquals("Breast", typeOfCancer.getShortName());
        Assert.assertEquals("tissue", typeOfCancer.getParent());

    }

    @Test
    public void getAllClinicalDataOfPatientInStudySingleStudySinglePatientNullAttributeSummaryProjection()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<String> patientIds = new ArrayList<>();
        patientIds.add("TCGA-A1-A0SB");
        List<PatientClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy(studyIds, patientIds, null, "SUMMARY",
                        null, null, null, null);

        Assert.assertEquals(3, result.size());
        PatientClinicalData data = result.get(0);
        Assert.assertEquals("FORM_COMPLETION_DATE", data.getAttrId());
        Assert.assertEquals("2013-12-5", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
        Assert.assertNull(data.getPatient());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudySingleStudySinglePatientWithAttributeSummaryProjection()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<String> patientIds = new ArrayList<>();
        patientIds.add("TCGA-A1-A0SB");
        List<PatientClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy(studyIds, patientIds,
                        "OTHER_PATIENT_ID", "SUMMARY", null, null, null, null);

        Assert.assertEquals(1, result.size());
        PatientClinicalData data = result.get(0);
        Assert.assertEquals("OTHER_PATIENT_ID", data.getAttrId());
        Assert.assertEquals("286CF147-B7F7-4A05-8E41-7FBD3717AD71", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
        Assert.assertNull(data.getPatient());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudySingleStudyMultiplePatientNullAttributeSummaryProjection()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("study_tcga_pub");
        List<String> patientIds = new ArrayList<>();
        patientIds.add("TCGA-A1-A0SB");
        patientIds.add("TCGA-A1-A0SD");
        List<PatientClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy(studyIds, patientIds, null, "SUMMARY",
                        null, null, null, null);

        Assert.assertEquals(4, result.size());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudyMultipleStudyMultiplePatientNullAttributeSummaryProjection()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("acc_tcga");
        List<String> patientIds = new ArrayList<>();
        patientIds.add("TCGA-A1-A0SB");
        patientIds.add("TCGA-A1-B0SO");
        List<PatientClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy(studyIds, patientIds, null, "SUMMARY",
                        null, null, null, null);

        Assert.assertEquals(7, result.size());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudySingleStudySinglePatientNullAttributeSummaryProjection2PageSize()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<String> patientIds = new ArrayList<>();
        patientIds.add("TCGA-A1-A0SB");
        List<PatientClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy(studyIds, patientIds, null, "SUMMARY",
                        2, 0, null, null);

        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudySingleStudySinglePatientNullAttributeSummaryProjectionAttrIdSort()
            throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB");
        List<PatientClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy(studyIds, sampleIds, null, "SUMMARY",
                        null, null, "attrId", "ASC");

        Assert.assertEquals(3, result.size());
        Assert.assertEquals("FORM_COMPLETION_DATE", result.get(0).getAttrId());
        Assert.assertEquals("OTHER_PATIENT_ID", result.get(1).getAttrId());
        Assert.assertEquals("RETROSPECTIVE_COLLECTION", result.get(2).getAttrId());
    }

    @Test
    public void getMetaPatientClinicalDataSingleStudyNullPatientNullAttribute() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        BaseMeta result = clinicalDataMyBatisRepository.getMetaPatientClinicalData(studyIds, null, null);
        Assert.assertEquals((Integer) 4, result.getTotalCount());
    }

    @Test
    public void getMetaPatientClinicalDataZeroCount() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("invalid_study");
        BaseMeta result = clinicalDataMyBatisRepository.getMetaPatientClinicalData(studyIds, null, null);
        Assert.assertEquals((Integer) 0, result.getTotalCount());
    }

    @Test
    public void getMetaPatientClinicalDataSingleStudySingleStudyeNullAttribute() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<String> patientIds = new ArrayList<>();
        patientIds.add("TCGA-A1-A0SB");
        BaseMeta result = clinicalDataMyBatisRepository.getMetaPatientClinicalData(studyIds, patientIds, null);
        Assert.assertEquals((Integer) 3, result.getTotalCount());
    }

    @Test
    public void getMetaPatientClinicalDataSingleStudySinglePatientWithAttribute() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        List<String> patientIds = new ArrayList<>();
        patientIds.add("TCGA-A1-A0SB");
        BaseMeta result = clinicalDataMyBatisRepository.getMetaPatientClinicalData(studyIds, patientIds,
                "OTHER_PATIENT_ID");

        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void getMetaPatientClinicalDataMultipleStudyMultiplePatientNullAttribute() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("acc_tcga");
        List<String> patientIds = new ArrayList<>();
        patientIds.add("TCGA-A1-A0SB");
        patientIds.add("TCGA-A1-B0SO");
        BaseMeta result = clinicalDataMyBatisRepository.getMetaPatientClinicalData(studyIds, patientIds, null);

        Assert.assertEquals((Integer) 7, result.getTotalCount());
    }
}