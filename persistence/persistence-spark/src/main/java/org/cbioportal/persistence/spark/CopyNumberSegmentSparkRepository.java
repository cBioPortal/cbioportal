package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CopyNumberSegmentRepository;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.cbioportal.persistence.spark.util.ParquetLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Qualifier("copyNumberSegmentSparkRepository")
public class CopyNumberSegmentSparkRepository implements CopyNumberSegmentRepository {

    @Autowired
    private SparkSession spark;

    @Autowired
    private ParquetLoader parquetLoader;

    @Override
    public List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId, String chromosome, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(String studyId, String sampleId, String chromosome) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> fetchSamplesWithCopyNumberSegments(List<String> studyIds, List<String> sampleIds, String chromosome) {
        throw new UnsupportedOperationException();
    }

    public List<Sample> fetchSamplesWithCopyNumberSegments(List<String> studyIds, List<String> sampleIds) {
        Dataset<Row> cnaSamples = parquetLoader.loadStudyFiles(
            spark, new HashSet<>(studyIds), ParquetConstants.CNA_SEG, false);
        if (!CollectionUtils.isEmpty(sampleIds)) {
            cnaSamples = cnaSamples.where(cnaSamples.col("ID").isin(sampleIds.toArray()));
        }
        cnaSamples = cnaSamples.select("ID").distinct();

        return cnaSamples.collectAsList().stream()
            .map(r -> mapToSample(r)).collect(Collectors.toList());
    }

    @Override
    public List<CopyNumberSeg> fetchCopyNumberSegments(List<String> studyIds, List<String> sampleIds, String chromosome, String projection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta fetchMetaCopyNumberSegments(List<String> studyIds, List<String> sampleIds, String chromosome) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(String studyId, String sampleListId, String chromosome, String projection) {
        throw new UnsupportedOperationException();
    }
    
    private Sample mapToSample(Row row) {
        Sample sample = new Sample();
        sample.setStableId(row.getString(0));
        return sample;
    }
}
