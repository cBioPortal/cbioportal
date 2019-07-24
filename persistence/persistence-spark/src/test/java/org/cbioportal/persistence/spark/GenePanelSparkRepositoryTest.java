package org.cbioportal.persistence.spark;

import org.apache.spark.sql.*;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.persistence.GeneRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Configurable
public class GenePanelSparkRepositoryTest {

    @Mock
    private SparkSession spark;

    @Mock
    private GeneRepository geneRepository;

    @Mock
    private ParquetLoader parquetLoader;

    @InjectMocks
    private GenePanelSparkRepository genePanelSparkRepository;

    private Dataset<Row> ds;

    @Before
    public void setup() {
        ds = mock(Dataset.class);
        when(parquetLoader.loadCaseListFiles(any(SparkSession.class), anySet(), anyBoolean())).thenReturn(ds);
        when(parquetLoader.loadGenePanelFiles(any(SparkSession.class), anyList())).thenReturn(ds);
    }

    @Test
    public void testFetchGenePanelDataInMultipleMolecularProfiles() {

        when(ds.select(anyString(), anyString())).thenReturn(ds);
        Column mockCol = mock(Column.class);
        List<Row> res = Arrays.asList(RowFactory.create("sampleId", "msk_impact_2017_cna"));
        when(ds.collectAsList()).thenReturn(res);

        List<GenePanelData> result = genePanelSparkRepository.fetchGenePanelDataInMultipleMolecularProfiles(
            Arrays.asList("msk_impact_2017_cna"), null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testFetchGenePanels() {
        when(spark.sql(anyString())).thenReturn(ds);
        List<Row> res = Arrays.asList(RowFactory.create("impact341", "impact341 description"));
        when(ds.collectAsList()).thenReturn(res);
        List<GenePanel> result = genePanelSparkRepository.fetchGenePanels(Arrays.asList("impact341"), PersistenceConstants.SUMMARY_PROJECTION);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void testGetGenesOfPanels() {
        when(ds.select(anyString(), anyString())).thenReturn(ds);
        when(ds.orderBy(anyString(), anyString())).thenReturn(ds);
        List<Row> res = Arrays.asList(RowFactory.create("impact341", "MYC"));
        when(ds.collectAsList()).thenReturn(res);

        List<Gene> genes = new ArrayList<>();
        Gene gene = new Gene();
        gene.setEntrezGeneId(1);
        gene.setHugoGeneSymbol("MYC");
        genes.add(gene);
        when(geneRepository.fetchGenesByHugoGeneSymbols(anyList(), anyString())).thenReturn(genes);

        List<GenePanelToGene> result = genePanelSparkRepository.getGenesOfPanels(Arrays.asList("impact341"));

        Assert.assertEquals(1, result.size());
        Assert.assertTrue(1 == result.get(0).getEntrezGeneId());
    }
}