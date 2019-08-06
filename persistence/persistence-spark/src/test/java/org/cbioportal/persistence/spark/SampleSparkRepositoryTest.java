package org.cbioportal.persistence.spark;

import org.apache.spark.sql.*;
import org.cbioportal.model.Sample;
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

    @Mock
    private ParquetLoader parquetLoader;
    
    @InjectMocks
    private SampleSparkRepository sampleSparkRepository;

    private Dataset<Row> ds;

    @Before
    public void setup() {

        ds = mock(Dataset.class);
        DataFrameReader dfr = mock(DataFrameReader.class);
        when(spark.sql(anyString())).thenReturn(ds);
        when(parquetLoader.loadStudyFiles(any(SparkSession.class), anySet(), anyString(), anyBoolean())).thenReturn(ds);
    }

    @Test
    public void testFetchSamplesSummary() {

        List<Row> res = Arrays.asList(
            RowFactory.create("patientId", "TCGA-AC-A6IX-01", "msk_impact_2017", "type"),
            RowFactory.create("patientId", "TCGA-AC-A6IX-02", "msk_impact_2017", "type"),
            RowFactory.create("patientId", "TCGA-AC-A6IX-03", "msk_impact_2017", "type"),
            RowFactory.create("patientId", "TCGA-AC-A6IX-04", "msk_impact_2017", "type"),
            RowFactory.create("patientId", "TCGA-AC-A6IX-06", "msk_impact_2017", "type"),
            RowFactory.create("patientId", "TCGA-AC-A6IX-10", "msk_impact_2017", "type"),
            RowFactory.create("patientId", "TCGA-AC-A6IX-11", "msk_impact_2017", "type"),
            RowFactory.create("patientId", "TCGA-AC-A6IX-19", "msk_impact_2017", "type"),
            RowFactory.create("patientId", "sampleId", "msk_impact_2017", "Solid Tissues Normal"),
            RowFactory.create("patientId", "sampleId", "msk_impact_2017", "type"));

        when(ds.collectAsList()).thenReturn(res);

        List<Sample> result = sampleSparkRepository.fetchSamples(Arrays.asList("msk_impact_2017"), null, "SUMMARY");

        Assert.assertEquals(10, result.size());
        Sample sample1 = result.get(0);
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, sample1.getSampleType());
        Assert.assertEquals("msk_impact_2017", sample1.getCancerStudyIdentifier());
        Sample sample2 = result.get(1);
        Assert.assertEquals(Sample.SampleType.RECURRENT_SOLID_TUMOR, sample2.getSampleType());
        Assert.assertEquals("msk_impact_2017", sample2.getCancerStudyIdentifier());
        Assert.assertEquals("TCGA-AC-A6IX-02", sample2.getStableId());
        Assert.assertEquals(Sample.SampleType.PRIMARY_BLOOD_TUMOR, result.get(2).getSampleType());
        Assert.assertEquals(Sample.SampleType.RECURRENT_BLOOD_TUMOR, result.get(3).getSampleType());
        Assert.assertEquals(Sample.SampleType.METASTATIC, result.get(4).getSampleType());
        Assert.assertEquals(Sample.SampleType.BLOOD_NORMAL, result.get(5).getSampleType());
        Assert.assertEquals(Sample.SampleType.SOLID_NORMAL, result.get(6).getSampleType());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, result.get(7).getSampleType());
        Assert.assertEquals(Sample.SampleType.SOLID_NORMAL, result.get(8).getSampleType());
        Assert.assertEquals(Sample.SampleType.PRIMARY_SOLID_TUMOR, result.get(9).getSampleType());
    }

    @Test
    public void testFetchSamplesId() {

        List<Row> res = Arrays.asList(RowFactory.create("P-1000", "P-1000-01", "msk_impact_2017"));
        when(ds.collectAsList()).thenReturn(res);

        List<Sample> result = sampleSparkRepository.fetchSamples(Arrays.asList("msk_impact_2017"), null, "ID");
        
        Assert.assertEquals(1, result.size());
        Sample sample = result.get(0);
        Assert.assertEquals("msk_impact_2017", sample.getCancerStudyIdentifier());
        Assert.assertEquals("P-1000-01", sample.getStableId());
        Assert.assertNull(sample.getSampleType());
    }
}