package org.cbioportal.persistence.spark.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.apache.spark.sql.*;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testSparkContext.xml")
@Configurable
public class LoadParquetTest {
    
    @Autowired
    private LoadParquet loadParquet;
    
    private static final String STUDY_ID = "msk_impact_2017";
    
    @Test
    public void testLoadTable() {
        String file = "sample";
        Dataset<Row> df = loadParquet.loadDataFile(STUDY_ID, file);
        
        df.printSchema();
        Assert.assertNotNull(df);
    }

    @Test
    public void testLoadTables() {
        String file = "sample";
        Dataset<Row> df = loadParquet.loadDataFiles(Arrays.asList(STUDY_ID), file);
        
        df.printSchema();
        Assert.assertNotNull(df);
    }
}
