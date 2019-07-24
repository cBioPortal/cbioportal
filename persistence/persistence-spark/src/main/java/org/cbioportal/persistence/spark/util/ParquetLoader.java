package org.cbioportal.persistence.spark.util;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.input_file_name;
import static org.apache.spark.sql.functions.udf;

@Component
public class ParquetLoader {

    @Value("${data.parquet.folder}")
    private String PARQUET_DIR;

    public Dataset<Row> loadStudyFiles(SparkSession spark, Set<String> studyIds, String file, boolean withStudyColumn) {
        List<String> studyIdArr = studyIds.stream()
            .map(s -> PARQUET_DIR + ParquetConstants.STUDIES_DIR + s + "/" + file).collect(Collectors.toList());
        Seq<String> fileSeq = JavaConverters.asScalaBuffer(studyIdArr).toSeq();

        if (withStudyColumn) {
            UserDefinedFunction getStudyId = udf((String fullPath) -> {
                String[] paths = fullPath.split("/");
                return paths[paths.length-3];
            }, DataTypes.StringType);

            return spark.read()
                .option("mergeSchema", true)
                .parquet(fileSeq)
                .withColumn("studyId", getStudyId.apply(input_file_name()));
        } else {
            return spark.read()
                .option("mergeSchema", true)
                .parquet(fileSeq);
        }
    }

    public Dataset<Row> loadCaseListFiles(SparkSession spark, Set<String> molecularProfileIds, boolean withStableId) {
        List<String> molecularProfileArr = molecularProfileIds.stream()
            .map(id -> PARQUET_DIR + ParquetConstants.CASE_LIST_DIR + id).collect(Collectors.toList());
        Seq<String> fileSeq = JavaConverters.asScalaBuffer(molecularProfileArr).toSeq();

        if (withStableId) {
            UserDefinedFunction getStudyId = udf((String fullPath) -> {
                String[] paths = fullPath.split("/");
                return paths[paths.length - 2];
            }, DataTypes.StringType);

            return spark.read()
                .option("mergeSchema", true)
                .parquet(fileSeq)
                .withColumn("molecularProfileId", getStudyId.apply(input_file_name()));
        } else {
            return spark.read()
                .option("mergeSchema", true)
                .parquet(fileSeq);
        }
    }
    
    public Dataset<Row> loadGenePanelFiles(SparkSession spark, List<String> genePanelFiles) {

        List<String> genePanelArr = genePanelFiles.stream()
            .map(id -> PARQUET_DIR + ParquetConstants.GENE_PANEL_DIR + id).collect(Collectors.toList());
        Seq<String> fileSeq = JavaConverters.asScalaBuffer(genePanelArr).toSeq();

        return spark.read()
            .option("mergeSchema", true)
            .parquet(fileSeq);
    }
}
