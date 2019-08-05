package org.cbioportal.persistence.spark;

import org.apache.spark.sql.*;
import org.cbioportal.model.*;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.spark.util.ParquetLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.apache.spark.sql.functions.abs;
import static org.apache.spark.sql.functions.col;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;


@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration("/testSparkContext.xml")
@TestPropertySource("/testPortal.properties")
@Configurable
public class ClinicalDataSparkRepositoryTest {

    @Mock
    private SparkSession spark;

    @Mock
    private ParquetLoader parquetLoader;
    
    @InjectMocks
    private ClinicalDataSparkRepository clinicalDataSparkRepository;

    private static final List<String> STUDY_IDS = Arrays.asList("msk_impact_2017");
    private static final List<String> SAMPLE_ATTR_IDS = Arrays.asList("CANCER_TYPE",
        "CANCER_TYPE_DETAILED", "SAMPLE_TYPE", "MATCHED_STATUS", "METASTATIC_SITE", "ONCOTREE_CODE",
        "PRIMARY_SITE", "SAMPLE_CLASS", "SAMPLE_COLLECTION_SOURCE", "SPECIMEN_PRESERVATION_TYPE");
    private static final List<String> PATIENT_ATTR_IDS = Arrays.asList("SAMPLE_COUNT", "SEX", "OS_STATUS", "VITAL_STATUS", "SMOKING_HISTORY");

    private DataFrameReader dfr;
    private Dataset<Row> ds;
    private Dataset<ClinicalDataModel> dm;
    private SQLContext sqlContext;
    private RelationalGroupedDataset gds;

    @Before
    public void setup() {

        dfr = mock(DataFrameReader.class);
        ds = mock(Dataset.class);
        dm = mock(Dataset.class);
        sqlContext = mock(SQLContext.class);
        gds = mock(RelationalGroupedDataset.class);
        when(parquetLoader.loadStudyFiles(any(SparkSession.class), anySet(), anyString(), anyBoolean())).thenReturn(ds);
        mockAssumptions();
    }

    private void mockAssumptions() {
        when(spark.sql(anyString())).thenReturn(ds);
        when(ds.withColumn(anyString(), any(Column.class))).thenReturn(ds);
        when(ds.filter(abs(col("`seg.mean`")).$greater$eq(0.2))).thenReturn(ds);
        when(ds.join(any(Dataset.class), anyString())).thenReturn(ds);
        when(ds.groupBy(anyString())).thenReturn(gds);
        when(gds.agg(any(Column.class))).thenReturn(ds);
        when(ds.drop(anyString())).thenReturn(ds);
        when(ds.unionByName(any(Dataset.class))).thenReturn(ds);
        when(ds.withColumnRenamed(anyString(), anyString())).thenReturn(ds);
        doNothing().when(ds).createOrReplaceTempView(anyString());
        String[] cols = {"col"};
        when(ds.columns()).thenReturn(cols);
        when(ds.as(any(Encoder.class))).thenReturn(dm);
        when(dm.collectAsList()).thenReturn(Arrays.asList(new ClinicalDataModel()));
        when(ds.collectAsList()).thenReturn(Arrays.asList(RowFactory.create(1L, "value", "attrId")));
    }

    @Test
    public void testFetchClinicalDataCountsSample() {

        String[] sampleArr = {"CANCER_TYPE",
            "CANCER_TYPE_DETAILED", "SAMPLE_TYPE", "MATCHED_STATUS", "METASTATIC_SITE", "ONCOTREE_CODE",
            "PRIMARY_SITE", "SAMPLE_CLASS", "SAMPLE_COLLECTION_SOURCE", "SPECIMEN_PRESERVATION_TYPE"};

        List<ClinicalDataCount> res = clinicalDataSparkRepository
            .fetchClinicalDataCounts(STUDY_IDS, null, SAMPLE_ATTR_IDS, PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE);
        Assert.assertNotNull(res);
    }

    @Test
    public void testFetchClinicalDataCountsNull() {

        List<ClinicalDataCount> res = clinicalDataSparkRepository
            .fetchClinicalDataCounts(STUDY_IDS, null, null, PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE);
        Assert.assertNotNull(res);
    }

    @Test
    public void testFetchClinicalDataCountsPatient() {
        String[] sampleArr = {"SAMPLE_COUNT", "SEX", "OS_STATUS", "VITAL_STATUS", "SMOKING_HISTORY"};
        List<ClinicalDataCount> res = clinicalDataSparkRepository
            .fetchClinicalDataCounts(STUDY_IDS, null, PATIENT_ATTR_IDS, "PATIENT");
        Assert.assertNotNull(res);
    }

    @Test
    public void testFetchClinicalData() {

        List<ClinicalData> res = clinicalDataSparkRepository
            .fetchClinicalData(STUDY_IDS, null, Arrays.asList("TUMOR_PURITY"),
                PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY");

        Assert.assertNotNull(res);
    }

    @Test
    public void testClinicalDataStatusMonths() {
        String[] samples = {"P-0000004","P-0000015"};
        when(ds.filter(col("PATIENT_ID").isin(samples)))
            .thenReturn(ds);

        List<ClinicalData> res = clinicalDataSparkRepository
            .fetchClinicalData(STUDY_IDS, Arrays.asList("P-0000004","P-0000015"), Arrays.asList("OS_STATUS", "OS_MONTHS"),
                "PATIENT", "SUMMARY");

        Assert.assertNotNull(res);
    }

    @Test
    public void testClinicalDataSample() {
        Column col = mock(Column.class);
        when(ds.col(anyString())).thenReturn(col);
        when(col.cast(anyString())).thenReturn(new Column("val"));
        when(col.divide(any(Column.class))).thenReturn(col);
        when(ds.drop(anyString(), anyString())).thenReturn(ds);

        List<ClinicalData> res = clinicalDataSparkRepository
            .fetchClinicalData(STUDY_IDS, null, Arrays.asList("DNA_INPUT", "MUTATION_COUNT",
                "FRACTION_GENOME_ALTERED","SAMPLE_COVERAGE"),
                PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE, "SUMMARY");

        Assert.assertNotNull(res);
    }

    @Test
    public void testClinicalDataPatient() {

        List<ClinicalData> res = clinicalDataSparkRepository
            .fetchClinicalData(STUDY_IDS, null, Arrays.asList("OS_MONTHS"),
                "PATIENT", "SUMMARY");

        Assert.assertNotNull(res);
    }
}