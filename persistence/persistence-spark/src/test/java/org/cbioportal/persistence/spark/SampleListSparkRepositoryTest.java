package org.cbioportal.persistence.spark;

import org.apache.spark.sql.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Configurable
public class SampleListSparkRepositoryTest {

    @Mock
    private SparkSession spark;

    @InjectMocks
    private SampleListSparkRepository sampleListSparkRepository;

    private Dataset<Row> ds;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        DataFrameReader dfr = mock(DataFrameReader.class);
        when(spark.read()).thenReturn(dfr);
        ds = mock(Dataset.class);
        when(dfr.parquet(anyString())).thenReturn(ds);
    }
    
    @Test
    public void testGetAllSampleIdsInSampleList() {
        
        when(ds.select(anyString())).thenReturn(ds);
        when(ds.collectAsList()).thenReturn(Arrays.asList(RowFactory.create("P1-sample")));
        List<String> result = sampleListSparkRepository.getAllSampleIdsInSampleList("msk_impact_2017_sequenced");

        Assert.assertEquals(1, result.size());
    }
}
