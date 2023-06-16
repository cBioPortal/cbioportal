package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PersistenceConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class ClinicalDataMyBatisRepositoryTest {

    private static int noPaging = 0;
    private static String noSearch = null;
    private static String noSort = null;

    List<String> studyIds = new ArrayList<>();
    List<String> sampleIds = new ArrayList<>();
    
    
    @Before
    public void init() {
        studyIds.add("study_tcga_pub");
        studyIds.add("study_tcga_pub");
        sampleIds.add("TCGA-A1-A0SB-01");
        sampleIds.add("TCGA-A1-A0SD-01");
    }
    
    @Autowired
    private ClinicalDataMyBatisRepository clinicalDataMyBatisRepository;

    @Test
    public void getAllClinicalDataOfSampleInStudyEmptyResult() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy("invalid_study",
            null, null, "ID", null, null, null, null);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudyNullAttributeSummaryProjection() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy("study_tcga_pub",
            "TCGA-A1-A0SB-01", null, "SUMMARY", null, null, null, null);

        Assert.assertEquals(4, result.size());
        ClinicalData data = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", data.getAttrId());
        Assert.assertEquals("276", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudyWithAttributeSummaryProjection() {

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
    public void getAllClinicalDataOfSampleInStudyNullAttributeSummaryProjection2PageSize() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy("study_tcga_pub",
            "TCGA-A1-A0SB-01", null, "SUMMARY", 2, 0, null, null);

        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudyNullAttributeSummaryProjectionAttrIdSort() {

        List<ClinicalData> result =
            clinicalDataMyBatisRepository.getAllClinicalDataOfSampleInStudy("study_tcga_pub", "TCGA-A1-A0SB-01",
                null, "SUMMARY", null, null, "attrId", "ASC");

        Assert.assertEquals(4, result.size());
        Assert.assertEquals("DAYS_TO_COLLECTION", result.get(0).getAttrId());
        Assert.assertEquals("IS_FFPE", result.get(1).getAttrId());
        Assert.assertEquals("OTHER_SAMPLE_ID", result.get(2).getAttrId());
    }

    @Test
    public void getMetaSampleClinicalDataZeroCount() {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaSampleClinicalData("invalid_study", null, null);
        Assert.assertEquals((Integer) 0, result.getTotalCount());
    }

    @Test
    public void getMetaSampleClinicalDataNullAttribute() {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaSampleClinicalData("study_tcga_pub", "TCGA-A1-A0SB-01",
            null);

        Assert.assertEquals((Integer) 4, result.getTotalCount());
    }

    @Test
    public void getMetaSampleClinicalDataWithAttribute() {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaSampleClinicalData("study_tcga_pub", "TCGA-A1-A0SB-01",
            "OTHER_SAMPLE_ID");

        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudyEmptyResult() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy("invalid_study",
            null, null, "ID", null, null, null, null);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudyNullAttributeSummaryProjection() {

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
    public void getAllClinicalDataOfPatientInStudyWithAttributeSummaryProjection() {

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
    public void getAllClinicalDataOfPatientInStudyNullAttributeSummaryProjection2PageSize() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy("study_tcga_pub",
            "TCGA-A1-A0SB", null, "SUMMARY", 2, 0, null, null);

        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudyNullAttributeSummaryProjectionAttrIdSort() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataOfPatientInStudy("study_tcga_pub",
            "TCGA-A1-A0SB", null, "SUMMARY", null, null, "attrId", "ASC");

        Assert.assertEquals(3, result.size());
        Assert.assertEquals("FORM_COMPLETION_DATE", result.get(0).getAttrId());
        Assert.assertEquals("OTHER_PATIENT_ID", result.get(1).getAttrId());
        Assert.assertEquals("RETROSPECTIVE_COLLECTION", result.get(2).getAttrId());
    }

    @Test
    public void getMetaPatientClinicalDataZeroCount() {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaPatientClinicalData("invalid_study", null, null);
        Assert.assertEquals((Integer) 0, result.getTotalCount());
    }

    @Test
    public void getMetaPatientClinicalDataNullAttribute() {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaPatientClinicalData("study_tcga_pub", "TCGA-A1-A0SB",
            null);

        Assert.assertEquals((Integer) 3, result.getTotalCount());
    }

    @Test
    public void getMetaPatientClinicalDataWithAttribute() {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaPatientClinicalData("study_tcga_pub", "TCGA-A1-A0SB",
            "OTHER_PATIENT_ID");

        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void getAllClinicalDataInStudyEmptyResult() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataInStudy("invalid_study",
            null, PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "ID", null, null, null, null);

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getAllClinicalDataInStudyNullAttributeSummaryProjection() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataInStudy("study_tcga_pub",
            null, PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY", null, null, null, null);

        Assert.assertEquals(8, result.size());
        ClinicalData data = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", data.getAttrId());
        Assert.assertEquals("276", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
    }

    @Test
    public void getAllClinicalDataInStudyWithAttributeSummaryProjection() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataInStudy("study_tcga_pub",
            "DAYS_TO_COLLECTION", PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY", null, null, null,
            null);

        Assert.assertEquals(2, result.size());
        Optional<ClinicalData> sample1DataOptional = result.stream().filter(r -> r.getSampleId().equals("TCGA-A1-A0SB-01")).findFirst();
        Optional<ClinicalData> sample2DataOptional = result.stream().filter(r -> r.getSampleId().equals("TCGA-A1-A0SD-01")).findFirst();
        Assert.assertTrue(sample1DataOptional.isPresent());
        Assert.assertTrue(sample2DataOptional.isPresent());
        final ClinicalData sample1Data = sample1DataOptional.get();
        final ClinicalData sample2Data = sample2DataOptional.get();
        
        Assert.assertEquals("DAYS_TO_COLLECTION", sample1Data.getAttrId());
        Assert.assertEquals("276", sample1Data.getAttrValue());
        Assert.assertEquals((Integer) 1, sample1Data.getInternalId());
        Assert.assertNull(sample1Data.getClinicalAttribute());
        
        Assert.assertEquals("DAYS_TO_COLLECTION", sample2Data.getAttrId());
        Assert.assertEquals("277", sample2Data.getAttrValue());
        Assert.assertEquals("277", sample2Data.getAttrValue());
        Assert.assertEquals((Integer) 2, sample2Data.getInternalId());
        Assert.assertNull(sample2Data.getClinicalAttribute());
    }

    @Test
    public void getAllClinicalDataInStudyNullAttributeSummaryProjection2PageSize() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataInStudy("study_tcga_pub",
            null, PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY", 2, 0, null, null);

        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getAllClinicalDataInStudyNullAttributeSummaryProjectionAttrIdSort() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.getAllClinicalDataInStudy("study_tcga_pub",
            null, PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY", null, null,
            "attrId", "ASC");

        Assert.assertEquals(8, result.size());
        List<String> attrIds = result.stream().map(r -> r.getAttrId()).distinct().collect(Collectors.toList());

        Assert.assertTrue(attrIds.contains("DAYS_TO_COLLECTION"));
        Assert.assertTrue(attrIds.contains("IS_FFPE"));
        Assert.assertTrue(attrIds.contains("OCT_EMBEDDED"));
        Assert.assertTrue(attrIds.contains("OTHER_SAMPLE_ID"));
        Assert.assertTrue(attrIds.contains("PATHOLOGY_REPORT_FILE_NAME"));
        Assert.assertTrue(attrIds.contains("SAMPLE_TYPE"));
    }

    @Test
    public void getMetaAllClinicalDataZeroCount() {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaAllClinicalData("invalid_study", null,
            PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 0, result.getTotalCount());
    }

    @Test
    public void getMetaAllClinicalDataNullAttribute() {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaAllClinicalData("study_tcga_pub", null,
            PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 8, result.getTotalCount());
    }

    @Test
    public void getMetaAllClinicalDataWithAttribute() {

        BaseMeta result = clinicalDataMyBatisRepository.getMetaAllClinicalData("study_tcga_pub", "DAYS_TO_COLLECTION",
            PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }

    @Test
    public void fetchAllClinicalDataInStudy() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.fetchAllClinicalDataInStudy("study_tcga_pub", sampleIds, null,
            PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY");

        Assert.assertEquals(8, result.size());
        ClinicalData data = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", data.getAttrId());
        Assert.assertEquals("276", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
    }

    @Test
    public void fetchMetaClinicalDataInStudy() {

        BaseMeta result = clinicalDataMyBatisRepository.fetchMetaClinicalDataInStudy("study_tcga_pub", sampleIds, null,
            PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 8, result.getTotalCount());
    }

    @Test
    public void fetchClinicalDataNullAttributeSummaryProjection() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.fetchClinicalData(studyIds, sampleIds, null,
            PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY");

        Assert.assertEquals(8, result.size());
        ClinicalData data = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", data.getAttrId());
        Assert.assertEquals("276", data.getAttrValue());
        Assert.assertEquals((Integer) 1, data.getInternalId());
        Assert.assertNull(data.getClinicalAttribute());
    }

    @Test
    public void fetchClinicalSampleDataClinicalTabPagingSuccess() {

        List<ClinicalData> resultFirstPage = clinicalDataMyBatisRepository.fetchSampleClinicalTable(studyIds,
            sampleIds, 1, 0, noSearch, noSort, "DESC");
        List<ClinicalData> resultSecondPage = clinicalDataMyBatisRepository.fetchSampleClinicalTable(studyIds,
            sampleIds, 1, 1, noSearch, noSort, "DESC");

        Assert.assertEquals(4, resultFirstPage.size());
        Assert.assertEquals(4, resultSecondPage.size());

        List<String> observedAttrIds = resultFirstPage.stream().map(e -> e.getAttrId()).collect(Collectors.toList());
        observedAttrIds.addAll(resultSecondPage.stream().map(e -> e.getAttrId()).collect(Collectors.toList()));

        List<String> expectedAttrIds = Arrays.asList("IS_FFPE", "OTHER_SAMPLE_ID", "OCT_EMBEDDED", "PATHOLOGY_REPORT_FILE_NAME",
            "DAYS_TO_COLLECTION", "SAMPLE_TYPE");

        Assert.assertTrue(
            "Paginated results do not contain the expected attribute ids." +
                " Expected: " + expectedAttrIds +
                " Observed: " + observedAttrIds,
            observedAttrIds.containsAll(expectedAttrIds)
        );
    }

    @Test
    public void fetchClinicalSampleDataClinicalTablePagingHandleNoneExistingPage() {

        // There are only two patients in total. The second page (index 1) with pageSize 2 does not refer to any records.
        List<ClinicalData> resultNonExistingPage = clinicalDataMyBatisRepository.fetchSampleClinicalTable(
            studyIds, sampleIds, 2, 1, noSearch, noSort, "DESC");

        Assert.assertEquals(0, resultNonExistingPage.size());
    }

    @Test
    public void fetchClinicalSampleDataClinicalTabSearchTermSuccess() {

        List<ClinicalData> resultSample1 = clinicalDataMyBatisRepository.fetchSampleClinicalTable(
            studyIds, sampleIds, noPaging, noPaging, "5C631CE8", noSort, "DESC");
            
        Assert.assertEquals(4, resultSample1.size());
        List<String> observedSampleIds = resultSample1.stream().map(s -> s.getSampleId()).distinct().collect(Collectors.toList());
        Assert.assertEquals(1, observedSampleIds.size());
        Assert.assertEquals("TCGA-A1-A0SB-01", observedSampleIds.get(0));

        List<ClinicalData> resultSample2 = clinicalDataMyBatisRepository.fetchSampleClinicalTable(
            studyIds, sampleIds, noPaging, noPaging, "F3408556-9259", noSort, "DESC");

        Assert.assertEquals(4, resultSample2.size());
        observedSampleIds = resultSample2.stream().map(s -> s.getSampleId()).distinct().collect(Collectors.toList());
        Assert.assertEquals(1, observedSampleIds.size());
        Assert.assertEquals("TCGA-A1-A0SD-01", observedSampleIds.get(0));
    }
    
    @Test
    public void fetchClinicalSampleDataEClinicalTabEmptyStringSearchTerm() {

        List<ClinicalData> result = clinicalDataMyBatisRepository.fetchSampleClinicalTable(
            studyIds, sampleIds, noPaging, noPaging, "", noSort, "DESC");
            
        Assert.assertEquals(8, result.size());
    }

    @Test
    public void fetchClinicalSampleDataClinicalTabSortSuccess() {

        List<ClinicalData> resultSortAsc = clinicalDataMyBatisRepository.fetchSampleClinicalTable(studyIds,
            sampleIds, 1, 0, noSearch, "SAMPLE_TYPE", "ASC");
        List<ClinicalData> resultSortDesc = clinicalDataMyBatisRepository.fetchSampleClinicalTable(studyIds,
            sampleIds, 1, 0, noSearch, "SAMPLE_TYPE", "DESC");

        Assert.assertEquals(4, resultSortAsc.size());
        Assert.assertEquals(4, resultSortDesc.size());

        List<String> observedSampleIdAsc = resultSortAsc.stream().map(e -> e.getSampleId()).distinct().collect(Collectors.toList());
        List<String> observedSampleIdDesc = resultSortDesc.stream().map(e -> e.getSampleId()).distinct().collect(Collectors.toList());

        Assert.assertEquals(1, observedSampleIdAsc.size());
        Assert.assertEquals(1, observedSampleIdDesc.size());
        Assert.assertEquals("TCGA-A1-A0SD-01", observedSampleIdAsc.get(0));
        Assert.assertEquals("TCGA-A1-A0SB-01", observedSampleIdDesc.get(0));

    }

    @Test
    public void fetchMetaClinicalDataNullAttribute() {

        BaseMeta result = clinicalDataMyBatisRepository.fetchMetaClinicalData(studyIds, sampleIds, null,
            PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 8, result.getTotalCount());
    }

    @Test
    public void fetchClinicalDataCounts() {

        List<ClinicalDataCount> result = clinicalDataMyBatisRepository.fetchClinicalDataCounts(Arrays.asList("acc_tcga", "acc_tcga"),
            Arrays.asList("TCGA-A1-B0SO-01", "TCGA-A1-A0SB-01"), Arrays.asList("OTHER_SAMPLE_ID",
                "DAYS_TO_COLLECTION"), "SAMPLE", "SUMMARY");

        Assert.assertEquals(2, result.size());
        ClinicalDataCount clinicalDataCount1 = result.get(0);
        Assert.assertEquals("DAYS_TO_COLLECTION", clinicalDataCount1.getAttributeId());
        Assert.assertEquals("111", clinicalDataCount1.getValue());
        Assert.assertEquals((Integer) 2, clinicalDataCount1.getCount());
        ClinicalDataCount clinicalDataCount2 = result.get(1);
        Assert.assertEquals("OTHER_SAMPLE_ID", clinicalDataCount2.getAttributeId());
        Assert.assertEquals("91E7F41C-17B3-4724-96EF-D3C207B964E1", clinicalDataCount2.getValue());
        Assert.assertEquals((Integer) 1, clinicalDataCount2.getCount());
    }
}
