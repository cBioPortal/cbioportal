package org.cbioportal.persistence.spark;

import org.apache.spark.sql.*;
import org.cbioportal.model.MutSig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration("/testSparkContext.xml")
@TestPropertySource("/testPortal.properties")
@Configurable
public class SignificantlyMutatedGeneSparkRepositoryTest {

    @Mock
    private SparkSession spark;

    @InjectMocks
    private SignificantlyMutatedGeneSparkRepository significantlyMutatedGeneSparkRepository;

    private Dataset<Row> ds;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ds = mock(Dataset.class);
        DataFrameReader dfr = mock(DataFrameReader.class);
        when(spark.read()).thenReturn(dfr);
        when(dfr.option(anyString(), anyBoolean())).thenReturn(dfr);
        when(dfr.parquet(anyString())).thenReturn(ds);
        when(spark.sql(anyString())).thenReturn(ds);
        List<Row> result = Arrays.asList(RowFactory.create("hugoGeneSymbol", "1", 1L, 1L));
        when(ds.collectAsList()).thenReturn(result);
    }

    @Test
    public void testGetSignificantlyMutatedGenes() {

        List<MutSig> res = significantlyMutatedGeneSparkRepository
            .getSignificantlyMutatedGenes("brca_tcga", "SUMMARY", null, null, null, null);
        Assert.assertNotNull(res);
    }

    @Test
    public void testGetSignificantlyMutatedGenesPagination() {
        when(ds.unionByName(any(Dataset.class))).thenReturn(ds);
        when(ds.drop(anyString())).thenReturn(ds);
        
        List<MutSig> res = significantlyMutatedGeneSparkRepository
            .getSignificantlyMutatedGenes("msk_impact_2017", "SUMMARY", 10, 2, null, null);
        Assert.assertNotNull(res);
    }
}