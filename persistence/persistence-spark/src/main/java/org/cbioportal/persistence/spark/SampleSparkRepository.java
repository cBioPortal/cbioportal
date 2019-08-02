package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.cbioportal.persistence.spark.util.ParquetLoader;
import org.cbioportal.persistence.spark.util.SampleTypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Qualifier("sampleSparkRepository")
public class SampleSparkRepository implements SampleRepository {

    @Autowired
    private SparkSession spark;

    @Autowired
    private ParquetLoader parquetLoader;

    @Override
    public List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                             String sortBy, String direction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta getMetaSamplesInStudy(String studyId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Sample> getAllSamplesInStudies(List<String> studyIds, String projection, Integer pageSize,
                                               Integer pageNumber, String sortBy, String direction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sample getSampleInStudy(String studyId, String sampleId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection,
                                                      Integer pageSize, Integer pageNumber, String sortBy,
                                                      String direction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Sample> getAllSamplesOfPatientsInStudy(String studyId, List<String> patientIds, String projection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Sample> getSamplesOfPatientsInMultipleStudies(List<String> studyIds, List<String> patientIds,
                                                              String projection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection) {

        Dataset<Row> samples = parquetLoader.loadStudyFiles(spark,
            new HashSet<>(studyIds), ParquetConstants.CLINICAL_SAMPLE, true);

        if (!CollectionUtils.isEmpty(sampleIds)) {
            samples = samples.where(samples.col("SAMPLE_ID").isin(sampleIds.toArray()));
        }

        samples.createOrReplaceTempView("samples");
        StringBuilder sb = new StringBuilder("SELECT PATIENT_ID, SAMPLE_ID, studyId");
        if (PersistenceConstants.SUMMARY_PROJECTION.equalsIgnoreCase(projection)
            || PersistenceConstants.DETAILED_PROJECTION.equalsIgnoreCase(projection)) {
            sb.append(", SAMPLE_TYPE");
        }
        sb.append(" FROM samples");

        Dataset<Row> res = spark.sql(sb.toString());

        return res.collectAsList().stream().
            map(r -> mapToSample(r, projection)).collect(Collectors.toList());
    }

    @Override
    public List<Sample> fetchSamples(List<String> sampleListIds, String projection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta fetchMetaSamples(List<String> sampleListIds) {
        throw new UnsupportedOperationException();    }

    @Override
    public List<Sample> getSamplesByInternalIds(List<Integer> internalIds) {
        throw new UnsupportedOperationException();
    }

    private Sample mapToSample(Row row, String projection) {
        Sample sample = new Sample();
        sample.setPatientStableId(row.getString(0));
        sample.setStableId(row.getString(1));
        sample.setCancerStudyIdentifier(row.getString(2));
        if (PersistenceConstants.SUMMARY_PROJECTION.equalsIgnoreCase(projection)
            || PersistenceConstants.DETAILED_PROJECTION.equalsIgnoreCase(projection)) {
            Sample.SampleType sampleType = SampleTypeUtil.getType(sample.getStableId(), row.getString(3));
            sample.setSampleType(sampleType);
        }
        return sample;
    }
}