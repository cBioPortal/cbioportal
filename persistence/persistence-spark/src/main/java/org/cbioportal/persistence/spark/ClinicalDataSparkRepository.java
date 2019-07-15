package org.cbioportal.persistence.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.spark.sql.*;
import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;


@Component
@Qualifier("clinicalDataSparkRepository")
public class ClinicalDataSparkRepository implements ClinicalDataRepository {

    @Autowired
    private SparkSession spark;

    @Value("${data.parquet.folder}")
    private String PARQUET_DIR;

    private static Log logger = LogFactory.getLog(ClinicalDataSparkRepository.class);
    private static final String MUTATION_COUNT = "MUTATION_COUNT";
    private static final String FRACTION_GENOME_ALTERED = "FRACTION_GENOME_ALTERED";

    @Override
    public List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId, String attributeId, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId, String attributeId, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId, String clinicalDataType, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClinicalData> fetchAllClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds, String clinicalDataType, String projection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta fetchMetaClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds, String clinicalDataType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> sampleIds, List<String> attributeIds,
                                                String clinicalDataType, String projection) {
        List<Dataset<Row>> res = new ArrayList<>();
        for (String studyId : new HashSet<>(studyIds)) {
            spark.sqlContext().setConf("spark.sql.caseSensitive", "false");
            String dataFile = ParquetConstants.CLINICAL_PATIENT;
            if (PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE.equalsIgnoreCase(clinicalDataType)) {
                dataFile = ParquetConstants.CLINICAL_SAMPLE;
            } 
            Dataset<Row> clinicalData = spark.read()
                .parquet(PARQUET_DIR + "/" + studyId + "/" + dataFile);

            if ("PATIENT".equalsIgnoreCase(clinicalDataType)) {
                Dataset<Row> clinicalSampleData = spark.read()
                    .parquet(PARQUET_DIR + "/" + studyId + "/" + ParquetConstants.CLINICAL_SAMPLE);
                clinicalData = clinicalData.join(clinicalSampleData, "PATIENT_ID");
            }

            // filter by sample id
            if (sampleIds != null && !sampleIds.isEmpty()) {
                String[] sampleArr = sampleIds.stream().toArray(String[]::new);
                clinicalData = clinicalData.filter(col("PATIENT_ID").isin(sampleArr).or(col("SAMPLE_ID").isin(sampleArr)));
            }

            // if attributes is null return data from all columns
            List<String> attributes = attributeIds;
            if (attributeIds == null || attributeIds.isEmpty()) {
                attributes = Arrays.asList(clinicalData.columns());
            }
            clinicalData.createOrReplaceTempView("clinicalData");

            for (String c : attributes) {
                StringBuilder sb = new StringBuilder();
                sb.append("SELECT PATIENT_ID as patientId, SAMPLE_ID as sampleId");
                if (!MUTATION_COUNT.equalsIgnoreCase(c) && !FRACTION_GENOME_ALTERED.equalsIgnoreCase(c)) {
                    sb.append(", " + c + " AS attrValue");
                }
                sb.append(" FROM clinicalData ");

                String query = sb.toString();
                Dataset<Row> sub = spark.sql(query);

                sub = sub.withColumn("attrId", lit(c))
                    .withColumn("studyId", lit(studyId));

                if (MUTATION_COUNT.equalsIgnoreCase(c)) {
                    sub = sub.join(getMutationCount(studyId), "sampleId");
                }
                if (FRACTION_GENOME_ALTERED.equalsIgnoreCase(c)) {
                    sub = sub.join(getFractionGenomeAltered(studyId), "sampleId");
                }
                res.add(sub);
            }
        }
        Dataset<Row> ds = res.get(0);
        for (Dataset<Row> r : res.subList(1, res.size())) {
            ds = ds.unionByName(r);
        }

        Dataset<ClinicalDataModel> clinicalDataModel = ds.as(Encoders.bean(ClinicalDataModel.class));
        List<ClinicalDataModel> dsls = clinicalDataModel.collectAsList();
        List<ClinicalData> clinicalData = dsls.stream()
            .map(r -> mapToClinicalData(r)).collect(Collectors.toList());
        return clinicalData;
    }

    @Override
    public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds, String clinicalDataType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClinicalDataCount> fetchClinicalDataCounts(List<String> studyIds, List<String> sampleIds,
                                                           List<String> attributeIds, String clinicalDataType) {
        List<Dataset<Row>> res = new ArrayList<>();

        spark.sqlContext().setConf("spark.sql.caseSensitive", "false");
        Set<String> studyIdSet = new HashSet<>(studyIds);
        String dataFile = ParquetConstants.CLINICAL_PATIENT;
        if (PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE.equalsIgnoreCase(clinicalDataType)) {
            dataFile = ParquetConstants.CLINICAL_SAMPLE;
        }
        Dataset<Row> clinicalData = loadDataFiles(studyIdSet, "/" + dataFile);

        if ("PATIENT".equalsIgnoreCase(clinicalDataType)) {
            Dataset<Row> clinicalSampleData = loadDataFiles(studyIdSet, ParquetConstants.CLINICAL_SAMPLE);
            clinicalData = clinicalData.join(clinicalSampleData, "PATIENT_ID");
        }

        // filter by sample id
        if (sampleIds != null && !sampleIds.isEmpty()) {
            String[] sampleArr = sampleIds.stream().toArray(String[]::new);
            clinicalData = clinicalData.filter(col("SAMPLE_ID").isin(sampleArr));
        }

        // if attributes is null return data from all columns
        List<String> attributes = attributeIds;
        if (attributeIds == null || attributeIds.isEmpty()) {
            attributes = Arrays.asList(clinicalData.columns());
        }

        clinicalData.createOrReplaceTempView("clinicalData");
        for (String c : attributes) {
            StringBuilder sb = new StringBuilder();
            if ("SAMPLE_COUNT".equalsIgnoreCase(c)) {
                sb.append("SELECT COUNT(*) AS count, countPatient AS value FROM ");
                sb.append("(SELECT COUNT(*) AS countPatient FROM clinicalData GROUP BY PATIENT_ID) AS SUB ");
                sb.append("GROUP BY countPatient");
            } else {
                sb.append("SELECT COUNT(*) AS count, ");
                sb.append(c + " AS value ");
                sb.append("FROM clinicalData ");
                sb.append(" GROUP BY ");
                sb.append(c);
            }

            String query = sb.toString();
            Dataset<Row> sub = spark.sql(query);

            sub = sub.withColumn("attributeId", lit(c));
            res.add(sub);
        }
        Dataset<Row> ds = res.get(0);
        for (Dataset<Row> r : res.subList(1, res.size())) {
            ds = ds.unionByName(r);
        }
        List<Row> dsls = ds.collectAsList();
        List<ClinicalDataCount> dataCount = dsls.stream()
            .map(r -> mapToClinicalDataCount(r)).collect(Collectors.toList());

        return dataCount;
    }

    private ClinicalDataCount mapToClinicalDataCount(Row row) {
        ClinicalDataCount cdc = new ClinicalDataCount();
        cdc.setCount((int) row.getLong(0));
        cdc.setValue(String.valueOf(row.get(1)));
        cdc.setAttributeId(String.valueOf(row.get(2)));

        return cdc;
    }

    private ClinicalData mapToClinicalData(ClinicalDataModel cdm) {
        ClinicalData cd = new ClinicalData();
        cd.setSampleId(cdm.getSampleId());
        cd.setPatientId(cdm.getPatientId());
        cd.setAttrId(cdm.getAttrId());
        cd.setStudyId(cdm.getStudyId());
        cd.setAttrValue(cdm.getAttrValue());

        return cd;
    }

    // Loads multiple tables with their schemas merged.
    public Dataset<Row> loadDataFiles(Set<String> studyIds, String file) {
        List<String> studyIdArr = studyIds.stream()
            .map(s -> PARQUET_DIR + "/" + s + "/" + file).collect(Collectors.toList());
        Seq<String> fileSeq = JavaConverters.asScalaBuffer(studyIdArr).toSeq();

        Dataset<Row> df = spark.read()
            .option("mergeSchema", true)
            .parquet(fileSeq);
        return df;
    }

    private Dataset<Row> getMutationCount(String studyId) {
        Dataset<Row> mutationDf = spark.read()
            .option("mergeSchema", true)
            .parquet(PARQUET_DIR + "/" + studyId + "/" + ParquetConstants.DATA_MUTATIONS);

        mutationDf = mutationDf.groupBy("Tumor_Sample_Barcode")
            .agg(countDistinct("Chromosome", "Start_Position", "End_Position",
                "Reference_Allele", "Tumor_Seq_Allele1").alias("attrValue"));
        mutationDf = mutationDf.withColumn("attrValue", mutationDf.col("attrValue").cast("string"));
        mutationDf = mutationDf.withColumnRenamed("Tumor_Sample_Barcode", "sampleId");
        return mutationDf;
    }

    private Dataset<Row> getFractionGenomeAltered(String studyId) {
        Dataset<Row> copyNumberSeg = spark.read()
            .parquet(PARQUET_DIR + "/" + studyId + "/" + ParquetConstants.CNA_SEG);

        Dataset<Row> copyNumberSegFiltered =copyNumberSeg
            .filter(abs(col("`seg.mean`")).$greater$eq(0.2)).groupBy("ID")
            .agg(sum(col("`loc.end`").minus(col("`loc.start`"))).alias("filteredSum"));

        copyNumberSeg = copyNumberSeg.groupBy("ID")
            .agg(sum(col("`loc.end`").minus(col("`loc.start`"))).alias("sumVal"));
        copyNumberSeg = copyNumberSeg.join(copyNumberSegFiltered, "ID");
        copyNumberSeg = copyNumberSeg.withColumn("attrValue",
            copyNumberSeg.col("filteredSum")
                .divide(copyNumberSeg.col("sumVal"))
                .cast("string"));
        copyNumberSeg = copyNumberSeg.withColumnRenamed("ID", "sampleId");
        copyNumberSeg = copyNumberSeg.drop("filteredSum", "sumVal");
        return copyNumberSeg;
    }
}