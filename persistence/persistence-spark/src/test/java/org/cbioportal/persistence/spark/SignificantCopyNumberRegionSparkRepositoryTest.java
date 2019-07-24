package org.cbioportal.persistence.spark;

import org.apache.spark.sql.*;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Gistic;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Configurable
public class SignificantCopyNumberRegionSparkRepositoryTest {

    @Mock
    private SparkSession spark;

    @Mock
    private GeneRepository geneRepository;
    
    @InjectMocks
    private SignificantCopyNumberRegionSparkRepository significantCopyNumberRegionSparkRepository;

    private Dataset<Row> ds;

    @Before
    public void setup() {
        ds = mock(Dataset.class);
        DataFrameReader dfr = mock(DataFrameReader.class);
        when(spark.read()).thenReturn(dfr);
        when(dfr.parquet(anyString())).thenReturn(ds);
        when(spark.sql(anyString())).thenReturn(ds);
        ReflectionTestUtils.setField(significantCopyNumberRegionSparkRepository, "PARQUET_DIR", "src/test/resources/parquet/");
    }
    
    @Test
    public void testGetSignificantCopyNumberRegions() {
        when(ds.withColumn(anyString(), any(Column.class))).thenReturn(ds);
        List<Gene> genes = new ArrayList<>();
        Gene g1 = new Gene();
        g1.setEntrezGeneId(1);
        g1.setHugoGeneSymbol("MYC");
        genes.add(g1);
        Gene g2 = new Gene();
        g2.setEntrezGeneId(1);
        g2.setHugoGeneSymbol("C1orf138");
        genes.add(g2);
        
        when(geneRepository.fetchGenesByHugoGeneSymbols(anyList(), anyString())).thenReturn(genes);

        List<Row> res = Arrays.asList(RowFactory.create("1", "1q21.3", "1", "4.7504e-42", "150563314", "150621176", "MYC,C1orf138,"));
        when(ds.select(anyString())).thenReturn(ds);
        when(ds.collectAsList()).thenReturn(res);
        
        List<Gistic> result = significantCopyNumberRegionSparkRepository.getSignificantCopyNumberRegions(
            "brca_tcga", PersistenceConstants.SUMMARY_PROJECTION, 1, 1, null, "ASC");

        Assert.assertEquals(1, result.size());
        Gistic gistic = result.get(0);
        Assert.assertTrue(gistic.getAmp());
        Assert.assertEquals("1q21.3", gistic.getCytoband());
        Assert.assertEquals(2, gistic.getGenes().size());


    }
}