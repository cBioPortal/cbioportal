package org.cbioportal.persistence.spark;

import org.apache.spark.sql.*;
import org.cbioportal.model.Sample;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import scala.collection.Seq;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration("/testSparkContext.xml")
@TestPropertySource("/testPortal.properties")
@Configurable
public class SampleSparkRepositoryTest {

    @Mock
    private SparkSession spark;

    @InjectMocks
    private SampleSparkRepository sampleSparkRepository;

    private Dataset<Row> ds;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ds = mock(Dataset.class);
        DataFrameReader dfr = mock(DataFrameReader.class);
        when(spark.read()).thenReturn(dfr);
        when(dfr.option(anyString(), anyBoolean())).thenReturn(dfr);
        when(dfr.parquet(any(Seq.class))).thenReturn(ds);
        when(spark.sql(anyString())).thenReturn(ds);
        when(ds.withColumn(anyString(), any(Column.class))).thenReturn(ds);
    }

    @Test
    public void testFetchSamplesSummary() {

        List<Row> res = Arrays.asList(RowFactory.create("patientId1", "sampleId1", "type1", "studyId"),
            RowFactory.create("patientId2", "TCGA-AC-A6IX-06", "type2", "studyId"));
        when(ds.collectAsList()).thenReturn(res);

        List<Sample> result = sampleSparkRepository.fetchSamples(Arrays.asList("msk_impact_2017"), null, "SUMMARY");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, result.get(0).getSampleType());
        Assert.assertEquals(Sample.SampleType.METASTATIC, result.get(1).getSampleType());
    }

    @Test
    public void testFetchSamplesId() {

        List<Row> res = Arrays.asList(RowFactory.create("patientId", "sampleId", "studyId"));
        when(ds.collectAsList()).thenReturn(res);
        when(ds.where(any(Column.class))).thenReturn(ds);
        when(ds.col(anyString())).thenReturn(mock(Column.class));

        List<Sample> result = sampleSparkRepository.fetchSamples(Arrays.asList("msk_impact_2017"), Arrays.asList("P-1000"), "ID");

        Assert.assertEquals(1, result.size());
    }
}