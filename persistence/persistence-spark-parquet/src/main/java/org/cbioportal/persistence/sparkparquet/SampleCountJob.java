package org.cbioportal.persistence.sparkparquet;

import org.apache.spark.sql.*;
import org.apache.livy.*;

public class SampleCountJob implements Job<Dataset> {
    @Override
    public Dataset call(JobContext jc) {
        try {
            SparkSession spark = jc.sparkSession();
            Dataset<Row> df = spark.read().parquet("file:///home/grossb/tmp/parquet-output-java/");
            df.createOrReplaceTempView("mutations");
            Dataset<Row> result = spark.sql("select first(Hugo_Symbol)," +
                                            "Entrez_Gene_Id," +
                                            "COUNT(*)," +
                                            "COUNT(DISTINCT(Tumor_Sample_Barcode)) " +
                                            "from mutations " +
                                            "group by Entrez_Gene_Id");
            return result;
        }
        catch (Exception e) {
            return null;
        }
    }
}
