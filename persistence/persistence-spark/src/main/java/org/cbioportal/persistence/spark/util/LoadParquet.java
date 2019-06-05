package org.cbioportal.persistence.spark.util;


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
    
    public Dataset<Row> loadDataFile(String studyId, String file) {
        spark.sqlContext().setConf("spark.sql.caseSensitive", "false");
        Dataset<Row> df = spark.read()
            .parquet(PARQUET_DIR + "/" + studyId + "/" + file + ".parquet");
        return df;
    }

    // Loads multiple tables with their schemas merged.
    public Dataset<Row> loadDataFiles(List<String> studyIds, String file) {
        studyIds = studyIds.stream()
            .map(s -> PARQUET_DIR + "/" + s + "/" + file + ".parquet").collect(Collectors.toList());
        Seq<String> fileSeq = JavaConverters.asScalaBuffer(studyIds).toSeq();
        
        Dataset<Row> df = spark.read().option("mergeSchema", true).parquet(fileSeq);
        return df;
    }
}