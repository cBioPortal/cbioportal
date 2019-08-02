package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.cbioportal.persistence.spark.util.ParquetLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class CopyNumberSegmentSparkRepository {

    @Autowired
    private SparkSession spark;

    @Autowired
    private ParquetLoader parquetLoader;
    
    public List<String> fetchSamplesWithCopyNumberSegments(List<String> studyIds, List<String> sampleIds) {
        Dataset<Row> cnaSamples = parquetLoader.loadStudyFiles(
            spark, new HashSet<>(studyIds), ParquetConstants.CNA_SEG, false);
        if (!CollectionUtils.isEmpty(sampleIds)) {
            cnaSamples = cnaSamples.where(cnaSamples.col("ID").isin(sampleIds.toArray()));
        }
        cnaSamples = cnaSamples.select("ID").distinct();

        return cnaSamples.collectAsList().stream()
            .map(r -> r.getString(0)).collect(Collectors.toList());
    }
}
