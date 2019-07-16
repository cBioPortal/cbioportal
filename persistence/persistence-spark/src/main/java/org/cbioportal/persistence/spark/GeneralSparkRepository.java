package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GeneralSparkRepository {

    @Autowired
    private SparkSession spark;

    @Value("${data.parquet.folder}")
    private String PARQUET_DIR;
    
    public List<String> fetchSamplesWithCopyNumberSegments(List<String> studyIds, List<String> sampleIds) {
        List<Dataset<Row>> res = new ArrayList<>();
        for (String studyId : new HashSet<>(studyIds)) {
            Dataset<Row> cnaSamples = spark.read()
                .parquet(PARQUET_DIR + "/" + studyId + "/" + ParquetConstants.CNA_SEG);

            if (sampleIds != null && !sampleIds.isEmpty()) {
                cnaSamples = cnaSamples
                    .where(cnaSamples.col("ID").isin(sampleIds.toArray()));
            }

            cnaSamples = cnaSamples.select("ID").distinct();
            res.add(cnaSamples);
        }
        Dataset<Row> ds = res.get(0);
        if (res.size() > 1) {
            for (Dataset<Row> sub: res.subList(1, res.size())) {
                ds = ds.unionByName(sub);
            }
        }

        List<Row> resls = ds.collectAsList();
        List<String> cnaSampleIds = resls.stream()
            .map(r -> r.getString(0)).collect(Collectors.toList());
        return cnaSampleIds;
    }
}
