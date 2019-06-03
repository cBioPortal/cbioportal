package org.cbioportal.persistence.spark.util;

//import org.mskcc.cbio.portal.util.SparkConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.spark.sql.*;

import scala.collection.Seq;
import scala.collection.JavaConverters;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class LoadParquet {
    
    @Autowired
    SparkSession spark;
    
    private static final String PARQUET_DIR = "src/main/resources/parquet/";
    
    public Dataset<Row> loadTable(String file) {
        spark.sqlContext().setConf("spark.sql.caseSensitive", "false");
        Dataset<Row> df = spark.read().parquet(PARQUET_DIR + file + ".parquet");
        return df;
    }

    // Loads multiple tables with their schemas merged.
    public Dataset<Row> loadTables(List<String> files) {
        files = files.stream()
            .map(f -> PARQUET_DIR + f + ".parquet").collect(Collectors.toList());
        Seq<String> fileSeq = JavaConverters.asScalaBuffer(files).toSeq();
        
        Dataset<Row> df = spark.read().option("mergeSchema", true).parquet(fileSeq);
        return df;
    }
}