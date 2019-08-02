package org.cbioportal.persistence.spark;

import org.apache.spark.sql.*;
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
public class CopyNumberSegmentSparkRepositoryTest {

    @Mock
    private SparkSession spark;

    @Mock
    private ParquetLoader parquetLoader;
    
    @InjectMocks
    private CopyNumberSegmentSparkRepository generalSparkRepository;

    private Dataset<Row> ds;

    @Before
    public void setup() {
        DataFrameReader dfr = mock(DataFrameReader.class);
        ds = mock(Dataset.class);
        when(parquetLoader.loadStudyFiles(any(SparkSession.class), anySet(), anyString(), anyBoolean())).thenReturn(ds);
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
