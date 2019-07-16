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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration("/testSparkContext.xml")
@TestPropertySource("/testPortal.properties")
@Configurable
public class GeneralSparkRepositoryTest {

    @Mock
    private SparkSession spark;

    @InjectMocks
    private GeneralSparkRepository generalSparkRepository;

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
    public void testFetchSamplesWithCopyNumberSegments() {
        when(ds.select(anyString())).thenReturn(ds);
        when(ds.distinct()).thenReturn(ds);

        List<Row> res = Arrays.asList(RowFactory.create("sample1"));
        when(ds.collectAsList()).thenReturn(res);
        
        List<String> cnaSampleIds = generalSparkRepository
            .fetchSamplesWithCopyNumberSegments(Arrays.asList("msk_impact_2017"), null);

        Assert.assertEquals(1, cnaSampleIds.size());
    }
}
