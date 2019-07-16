package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataTypes;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;

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

        Dataset<Row> samples = loadStudyFiles(new HashSet<>(studyIds), ParquetConstants.CLINICAL_SAMPLE);
        
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

    private Dataset<Row> loadStudyFiles(Set<String> studyIds, String file) {
        List<String> studyIdArr = studyIds.stream()
            .map(s -> PARQUET_DIR + ParquetConstants.STUDIES_DIR + s + "/" + file).collect(Collectors.toList());
        Seq<String> fileSeq = JavaConverters.asScalaBuffer(studyIdArr).toSeq();

        UserDefinedFunction getStudyId = udf((String fullPath) -> {
            String[] paths = fullPath.split("/");
            return paths[paths.length-3];
        }, DataTypes.StringType);

        return spark.read()
            .option("mergeSchema", true)
            .parquet(fileSeq)
            .withColumn("studyId", getStudyId.apply(input_file_name()));
    }
    
    private Sample mapToSample(Row row, String projection) {
        Sample sample = new Sample();
        sample.setPatientStableId(row.getString(0));
        sample.setStableId(row.getString(1));
        String typeOrStudy = row.getString(2);
        if (PersistenceConstants.SUMMARY_PROJECTION.equalsIgnoreCase(projection)
            || PersistenceConstants.DETAILED_PROJECTION.equalsIgnoreCase(projection)) {
            Sample.SampleType sampleType = getType(sample.getStableId(), typeOrStudy);
            sample.setSampleType(sampleType);
            sample.setCancerStudyIdentifier(row.getString(3));
        } else {
            sample.setCancerStudyIdentifier(typeOrStudy);

        }
        return sample;
    }

    private Sample.SampleType getType(String stableId, String sampleType) {
        Matcher tcgaSampleBarcodeMatcher = Pattern.compile("^TCGA-\\w\\w-\\w\\w\\w\\w-(\\d\\d).*$").matcher(stableId);
        if (tcgaSampleBarcodeMatcher.find()) {
            String tcgaCode =tcgaSampleBarcodeMatcher.group(1);
            if (tcgaCode.equals("01")) {
                return Sample.SampleType.PRIMARY_SOLID_TUMOR;
            }
            else if (tcgaCode.equals("02")) {
                return Sample.SampleType.RECURRENT_SOLID_TUMOR;
            }
            else if (tcgaCode.equals("03")) {
                return Sample.SampleType.PRIMARY_BLOOD_TUMOR;
            }
            else if (tcgaCode.equals("04")) {
                return Sample.SampleType.RECURRENT_BLOOD_TUMOR;
            }
            else if (tcgaCode.equals("06")) {
                return Sample.SampleType.METASTATIC;
            }
            else if (tcgaCode.equals("10")) {
                return Sample.SampleType.BLOOD_NORMAL;
            }
            else if (tcgaCode.equals("11")) {
                return Sample.SampleType.SOLID_NORMAL;
            }
            else {
                return Sample.SampleType.PRIMARY_SOLID_TUMOR;
            }
        }
        else if (sampleType != null && Sample.SampleType.fromString(sampleType) != null) {
            return Sample.SampleType.fromString(sampleType.toUpperCase());
        }
        else {
            return Sample.SampleType.PRIMARY_SOLID_TUMOR;
        }
    }
}