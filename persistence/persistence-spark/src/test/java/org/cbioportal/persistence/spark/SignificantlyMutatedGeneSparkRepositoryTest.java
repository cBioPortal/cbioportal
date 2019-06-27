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

    private static final String STUDY_ID = "brca_tcga";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testGetSignificantlyMutatedGenes() {
        DataFrameReader dfr = mock(DataFrameReader.class);
        when(spark.read()).thenReturn(dfr);
        when(dfr.parquet(anyString())).thenReturn(mock(Dataset.class));
        when(spark.sql(anyString())).thenReturn(mock(Dataset.class));

        List<MutSig> res = significantlyMutatedGeneSparkRepository
            .getSignificantlyMutatedGenes(STUDY_ID, "SUMMARY", null, null, null, null);
        Assert.assertNotNull(res);
    }
}