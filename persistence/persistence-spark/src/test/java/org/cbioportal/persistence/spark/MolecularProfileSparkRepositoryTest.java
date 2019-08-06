package org.cbioportal.persistence.spark;

import org.apache.spark.sql.*;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.persistence.spark.util.ParquetLoader;
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
public class MolecularProfileSparkRepositoryTest {

    @Mock
    private SparkSession spark;

    @Mock
    private ParquetLoader parquetLoader;
    
    @InjectMocks
    private MolecularProfileSparkRepository molecularProfileSparkRepository;

    private Dataset<Row> ds;

    @Before
    public void setup() {
        ds = mock(Dataset.class);
        DataFrameReader dfr = mock(DataFrameReader.class);
        when(ds.select(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(ds);
        DataFrameNaFunctions dfna = mock(DataFrameNaFunctions.class);
        when(ds.na()).thenReturn(dfna);
        when(dfna.drop()).thenReturn(ds);
        when(parquetLoader.loadStudyFiles(any(SparkSession.class), anySet(), anyString(), anyBoolean())).thenReturn(ds);
    }

    @Test
    public void testGetMolecularProfilesInStudies() {
        when(ds.collectAsList()).thenReturn(Arrays.asList(RowFactory.create("cancer_study_identifier",
            "MUTATION_EXTENDED", "datatype", "stable_id", "show_profile_in_analysis_tab", 
            "profile_name", "profile_description")));
        
        List<MolecularProfile> result = molecularProfileSparkRepository
            .getMolecularProfilesInStudies(Arrays.asList("msk_impact_2017"), "SUMMARY");

        Assert.assertEquals(1, result.size());
    }
}
