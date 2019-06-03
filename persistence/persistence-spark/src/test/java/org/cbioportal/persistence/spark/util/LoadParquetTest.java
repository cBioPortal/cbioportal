package org.cbioportal.persistence.spark.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.apache.spark.sql.*;

import java.util.List;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testSparkContext.xml")
@Configurable
public class LoadParquetTest {
    
    @Autowired
    private LoadParquet loadParquet;
    
    @Test
    public void testLoadTable() {
        String file = "users";
        Dataset<Row> df = loadParquet.loadTable(file);
        
        df.printSchema();
        Assert.assertNotNull(df);
    }

    @Test
    public void testLoadTables() {
        List<String> files = Arrays.asList("sample", "patient", "cancer_study");
        Dataset<Row> df = loadParquet.loadTables(files);
        
        df.printSchema();
        Assert.assertNotNull(df);
    }
}
