package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PersistenceConstants;
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
    public void getAllClinicalDataOfSampleInStudyEmptyResult() throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy("invalid_study",
                null, null, "ID", null, null, null, null);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudyNullAttributeSummaryProjection()
            throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy("study_tcga_pub",
                "TCGA-A1-A0SB-01", null, "SUMMARY", null, null, null, null);

        Assert.assertEquals(3, result.size());
        ClinicalData data = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", data.getAttrId());
        Assert.assertEquals("276", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudyWithAttributeSummaryProjection()
            throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy("study_tcga_pub",
                "TCGA-A1-A0SB-01", "OTHER_SAMPLE_ID", "SUMMARY", null, null, null, null);

        Assert.assertEquals(1, result.size());
        ClinicalData data = result.get(0);
        Assert.assertEquals("OTHER_SAMPLE_ID", data.getAttrId());
        Assert.assertEquals("5C631CE8-F96A-4C35-A459-556FC4AB21E1", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudyNullAttributeSummaryProjection2PageSize()
            throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy("study_tcga_pub",
                "TCGA-A1-A0SB-01", null, "SUMMARY", 2, 0, null, null);

        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudyNullAttributeSummaryProjectionAttrIdSort()
            throws Exception {

        List<ClinicalData> result =
                clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy("study_tcga_pub", "TCGA-A1-A0SB-01",
                        null, "SUMMARY", null, null, "attrId", "ASC");

        Assert.assertEquals(3, result.size());
        Assert.assertEquals("DAYS_TO_COLLECTION", result.get(0).getAttrId());
        Assert.assertEquals("IS_FFPE", result.get(1).getAttrId());
        Assert.assertEquals("OTHER_SAMPLE_ID", result.get(2).getAttrId());
    }

    @Test
    public void getMetaSampleClinicalDataZeroCount() throws Exception {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaSampleClinicalData("invalid_study", null, null);
        Assert.assertEquals((Integer) 0, result.getTotalCount());
    }

    @Test
    public void getMetaSampleClinicalDataNullAttribute() throws Exception {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaSampleClinicalData("study_tcga_pub", "TCGA-A1-A0SB-01",
                null);

        Assert.assertEquals((Integer) 3, result.getTotalCount());
    }

    @Test
    public void getMetaSampleClinicalDataWithAttribute() throws Exception {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaSampleClinicalData("study_tcga_pub", "TCGA-A1-A0SB-01",
                "OTHER_SAMPLE_ID");

        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudyEmptyResult() throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy("invalid_study",
                null, null, "ID", null, null, null, null);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudyNullAttributeSummaryProjection()
            throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy("study_tcga_pub",
                "TCGA-A1-A0SB", null, "SUMMARY", null, null, null, null);

        Assert.assertEquals(3, result.size());
        ClinicalData data = result.get(0);
        Assert.assertEquals("FORM_COMPLETION_DATE", data.getAttrId());
        Assert.assertEquals("2013-12-5", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudyWithAttributeSummaryProjection()
            throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy("study_tcga_pub",
                "TCGA-A1-A0SB", "OTHER_PATIENT_ID", "SUMMARY", null, null, null, null);

        Assert.assertEquals(1, result.size());
        ClinicalData data = result.get(0);
        Assert.assertEquals("OTHER_PATIENT_ID", data.getAttrId());
        Assert.assertEquals("286CF147-B7F7-4A05-8E41-7FBD3717AD71", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudyNullAttributeSummaryProjection2PageSize()
            throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy("study_tcga_pub",
                "TCGA-A1-A0SB", null, "SUMMARY", 2, 0, null, null);

        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudyNullAttributeSummaryProjectionAttrIdSort()
            throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy("study_tcga_pub",
                "TCGA-A1-A0SB", null, "SUMMARY", null, null, "attrId", "ASC");

        Assert.assertEquals(3, result.size());
        Assert.assertEquals("FORM_COMPLETION_DATE", result.get(0).getAttrId());
        Assert.assertEquals("OTHER_PATIENT_ID", result.get(1).getAttrId());
        Assert.assertEquals("RETROSPECTIVE_COLLECTION", result.get(2).getAttrId());
    }

    @Test
    public void getMetaPatientClinicalDataZeroCount() throws Exception {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaPatientClinicalData("invalid_study", null, null);
        Assert.assertEquals((Integer) 0, result.getTotalCount());
    }

    @Test
    public void getMetaPatientClinicalDataNullAttribute() throws Exception {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaPatientClinicalData("study_tcga_pub", "TCGA-A1-A0SB",
                null);

        Assert.assertEquals((Integer) 3, result.getTotalCount());
    }

    @Test
    public void getMetaPatientClinicalDataWithAttribute() throws Exception {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaPatientClinicalData("study_tcga_pub", "TCGA-A1-A0SB",
                "OTHER_PATIENT_ID");

        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void getAllClinicalDataInStudyEmptyResult() throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataInStudy("invalid_study",
                null, PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "ID", null, null, null, null);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getAllClinicalDataInStudyNullAttributeSummaryProjection() throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataInStudy("study_tcga_pub",
                null, PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY", null, null, null, null);

        Assert.assertEquals(6, result.size());
        ClinicalData data = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", data.getAttrId());
        Assert.assertEquals("276", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
    }

    @Test
    public void getAllClinicalDataInStudyWithAttributeSummaryProjection() throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataInStudy("study_tcga_pub",
                "DAYS_TO_COLLECTION", PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY", null, null, null,
                null);

        Assert.assertEquals(1, result.size());
        ClinicalData data = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", data.getAttrId());
        Assert.assertEquals("276", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
    }

    @Test
    public void getAllClinicalDataInStudyNullAttributeSummaryProjection2PageSize() throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataInStudy("study_tcga_pub",
                null, PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY", 2, 0, null, null);

        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getAllClinicalDataInStudyNullAttributeSummaryProjectionAttrIdSort() throws Exception {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataInStudy("study_tcga_pub",
                null, PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY", null, null, "attrId", "ASC");

        Assert.assertEquals(6, result.size());
        Assert.assertEquals("DAYS_TO_COLLECTION", result.get(0).getAttrId());
        Assert.assertEquals("IS_FFPE", result.get(1).getAttrId());
        Assert.assertEquals("OCT_EMBEDDED", result.get(2).getAttrId());
        Assert.assertEquals("OTHER_SAMPLE_ID", result.get(3).getAttrId());
        Assert.assertEquals("PATHOLOGY_REPORT_FILE_NAME", result.get(4).getAttrId());
        Assert.assertEquals("SAMPLE_TYPE", result.get(5).getAttrId());
    }

    @Test
    public void getMetaAllClinicalDataZeroCount() throws Exception {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaAllClinicalData("invalid_study", null,
                PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 0, result.getTotalCount());
    }

    @Test
    public void getMetaAllClinicalDataNullAttribute() throws Exception {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaAllClinicalData("study_tcga_pub", null,
                PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 6, result.getTotalCount());
    }

    @Test
    public void getMetaAllClinicalDataWithAttribute() throws Exception {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaAllClinicalData("study_tcga_pub", "DAYS_TO_COLLECTION",
                PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void fetchClinicalDataNullAttributeSummaryProjection() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-A0SD-01");
        List<ClinicalData> result = clinicalDataMyBatisRepository.fetchClinicalData(studyIds, sampleIds, null,
                PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY");

        Assert.assertEquals(6, result.size());
        ClinicalData data = result.get(0);
        Assert.assertEquals("OTHER_SAMPLE_ID", data.getAttrId());
        Assert.assertEquals("5C631CE8-F96A-4C35-A459-556FC4AB21E1", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
    }

    @Test
    public void fetchMetaClinicalDataNullAttribute() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_tcga_pub");
        studyIds.add("study_tcga_pub");
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-A0SD-01");
        BaseMeta result = clinicalDataMyBatisRepository.fetchMetaClinicalData(studyIds, sampleIds, null,
                PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 6, result.getTotalCount());
    }
}