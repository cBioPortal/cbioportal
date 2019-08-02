package org.cbioportal.persistence.spark;

import org.apache.spark.sql.*;
import org.cbioportal.model.GenePanelData;
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
public class GenePanelSparkRepositoryTest {
    
    @Mock
    private SparkSession spark;

    @Mock
    private ParquetLoader parquetLoader;
    
    @InjectMocks
    private GenePanelSparkRepository genePanelSparkRepository;

    private Dataset<Row> ds;

    @Before
    public void setup() {
        DataFrameReader dfr = mock(DataFrameReader.class);
        ds = mock(Dataset.class);
        when(parquetLoader.loadCaseListFiles(any(SparkSession.class), anySet(), anyBoolean())).thenReturn(ds);
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
}
