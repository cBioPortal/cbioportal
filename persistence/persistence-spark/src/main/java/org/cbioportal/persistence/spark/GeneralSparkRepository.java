package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class GeneralSparkRepository {

    @Autowired
    private SparkSession spark;

    @Value("${data.parquet.folder}")
    private String PARQUET_DIR;
    
    public List<String> fetchSamplesWithCopyNumberSegments(List<String> studyIds, List<String> sampleIds) {
        Dataset<Row> cnaSamples =loadStudyFiles(new HashSet<>(studyIds), ParquetConstants.CNA_SEG);
        if (!CollectionUtils.isEmpty(sampleIds)) {
            cnaSamples = cnaSamples.where(cnaSamples.col("ID").isin(sampleIds.toArray()));
        }
        cnaSamples = cnaSamples.select("ID").distinct();

        return cnaSamples.collectAsList().stream()
            .map(r -> r.getString(0)).collect(Collectors.toList());
    }

    // Loads multiple tables with their schemas merged.
    public Dataset<Row> loadStudyFiles(Set<String> studyIds, String file) {
        List<String> studyIdArr = studyIds.stream()
            .map(s -> PARQUET_DIR + ParquetConstants.STUDIES_DIR + s + "/" + file).collect(Collectors.toList());
        Seq<String> fileSeq = JavaConverters.asScalaBuffer(studyIdArr).toSeq();

        return spark.read()
            .option("mergeSchema", true)
            .parquet(fileSeq);
    }
}
