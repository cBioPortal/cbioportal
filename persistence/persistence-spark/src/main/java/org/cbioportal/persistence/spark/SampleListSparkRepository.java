package org.cbioportal.persistence.spark;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.model.SampleList;
import org.cbioportal.model.SampleListToSampleId;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Qualifier("sampleListSparkRepository")
public class SampleListSparkRepository implements SampleListRepository {

    @Autowired
    private SparkSession spark;

    @Value("${data.parquet.folder}")
    private String PARQUET_DIR;
    
    @Override
    public List<SampleList> getAllSampleLists(String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        throw new UnsupportedOperationException();    }

    @Override
    public BaseMeta getMetaSampleLists() {
        throw new UnsupportedOperationException();    }

    @Override
    public SampleList getSampleList(String sampleListId) {
        throw new UnsupportedOperationException();    }

    @Override
    public List<SampleList> getSampleLists(List<String> sampleListIds, String projection) {
        throw new UnsupportedOperationException();    }

    @Override
    public List<SampleList> getAllSampleListsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        throw new UnsupportedOperationException();    }

    @Override
    public BaseMeta getMetaSampleListsInStudy(String studyId) {
        throw new UnsupportedOperationException();    }

    @Override
    public List<String> getAllSampleIdsInSampleList(String sampleListId) {

        Dataset<Row> sampleList = spark.read()
            .parquet(PARQUET_DIR + ParquetConstants.CASE_LIST_DIR + sampleListId + ".txt.parquet");
        sampleList = sampleList.select("case_list_ids");
        List<Row> sampleListRows = sampleList.collectAsList();
        List<String> sampleListStrings = sampleListRows.stream()
            .map(r -> r.getString(0)).collect(Collectors.toList());
        return sampleListStrings;
    }

    @Override
    public List<SampleListToSampleId> getSampleListSampleIds(List<Integer> sampleListIds) {
        throw new UnsupportedOperationException();    }
}
