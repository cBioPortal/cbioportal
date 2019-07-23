package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.lit;

@Component
@Qualifier("sampleSparkRepository")
public class SampleSparkRepository implements SampleRepository {

    @Autowired
    private SparkSession spark;

    @Value("${data.parquet.folder}")
    private String PARQUET_DIR;

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
        List<Dataset<Row>> res = new ArrayList<>();
        for (String studyId : new HashSet<>(studyIds)) {
            Dataset<Row> samples = spark.read()
                .parquet(PARQUET_DIR + ParquetConstants.STUDIES_DIR + studyId + "/" + ParquetConstants.DATA_CLINICAL_SAMPLE);
            
            samples.createOrReplaceTempView("samples");
            StringBuilder sb = new StringBuilder("SELECT PATIENT_ID, SAMPLE_ID");
            if (PersistenceConstants.SUMMARY_PROJECTION.equalsIgnoreCase(projection)
                || PersistenceConstants.DETAILED_PROJECTION.equalsIgnoreCase(projection)) {
                sb.append(", SAMPLE_TYPE");
            }
            sb.append(" FROM samples");
            
            Dataset<Row> sub = spark.sql(sb.toString());
            if (sampleIds != null && !sampleIds.isEmpty()) {
                sub = sub.where(sub.col("SAMPLE_ID").isin(sampleIds.toArray()));
            }
            sub = sub.withColumn("cancerStudyId", lit(studyId));
            res.add(sub);
        }
        Dataset<Row> ds = res.get(0);
        if (res.size() > 1) {
            for (Dataset<Row> sub: res.subList(1, res.size())) {
                ds = ds.unionByName(sub);
            }
        }
        
        List<Row> resls = ds.collectAsList();
        List<Sample> samplels = resls.stream().
            map(r -> mapToSample(r, projection)).collect(Collectors.toList());
        return samplels;
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
        String typeOrStudy = row.getString(2);
        if (PersistenceConstants.SUMMARY_PROJECTION.equalsIgnoreCase(projection)
            || PersistenceConstants.DETAILED_PROJECTION.equalsIgnoreCase(projection)) {
            Sample.SampleType sampleType = "Metastasis".equalsIgnoreCase(typeOrStudy) ?
                Sample.SampleType.METASTATIC : Sample.SampleType.PRIMARY_SOLID_TUMOR;
            sample.setSampleType(sampleType);
            sample.setCancerStudyIdentifier(row.getString(3));
        } else {
            sample.setCancerStudyIdentifier(typeOrStudy);

        }
        return sample;
    }
}
