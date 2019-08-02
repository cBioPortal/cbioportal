package org.cbioportal.persistence.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataTypes;
import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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
    public List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
                                                String clinicalDataType, String projection) {
        List<Dataset<Row>> res = new ArrayList<>();

        Set<String> studyIdSet = new HashSet<>(studyIds);
        String dataFile = ParquetConstants.CLINICAL_PATIENT;
        if (PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE.equalsIgnoreCase(clinicalDataType)) {
            dataFile = ParquetConstants.CLINICAL_SAMPLE;
        }
        Dataset<Row> clinicalData = loadStudyFiles(studyIdSet, dataFile);

        // join with clinical sample data to get sample id information.
        if (PersistenceConstants.PATIENT_CLINICAL_DATA_TYPE.equalsIgnoreCase(clinicalDataType)) {
            Dataset<Row> clinicalSampleData = loadStudyFiles(studyIdSet, ParquetConstants.CLINICAL_SAMPLE);
            clinicalData = clinicalData.join(clinicalSampleData.drop("studyId"), "PATIENT_ID");
        }

        // filter by sample id
        if (!CollectionUtils.isEmpty(ids)) {
            String[] caseArr = ids.stream().toArray(String[]::new);
            if (PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE.equalsIgnoreCase(clinicalDataType)) {
                clinicalData = clinicalData.filter(col("SAMPLE_ID").isin(caseArr));
            } else {
                clinicalData = clinicalData.filter(col("PATIENT_ID").isin(caseArr));
            }
        }

        // if attributes is null return data from all columns
        List<String> attributes = attributeIds;
        if (CollectionUtils.isEmpty(attributeIds)) {
            attributes = Arrays.asList(clinicalData.columns());
        }
        clinicalData.createOrReplaceTempView("clinicalData");

        for (String attributeId : attributes) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT PATIENT_ID as patientId, SAMPLE_ID as sampleId, studyId");
            if (!MUTATION_COUNT.equalsIgnoreCase(attributeId) && !FRACTION_GENOME_ALTERED.equalsIgnoreCase(attributeId)) {
                sb.append(", " + attributeId + " AS attrValue");
                sb.append(", '" + attributeId + "' AS attrId");
            }
            sb.append(" FROM clinicalData ");

            String query = sb.toString();
            Dataset<Row> sub = spark.sql(query);

            if (MUTATION_COUNT.equalsIgnoreCase(attributeId)) {
                sub = sub.join(getMutationCount(studyIdSet), "sampleId");
            }
            if (FRACTION_GENOME_ALTERED.equalsIgnoreCase(attributeId)) {
                sub = sub.join(getFractionGenomeAltered(studyIdSet), "sampleId");
            }
            res.add(sub);
        }

        Dataset<Row> ds = res.get(0);
        // union all datasets before calling collectAsList which move all  data into the application's driver process.
        for (Dataset<Row> r : res.subList(1, res.size())) {
            ds = ds.unionByName(r);
        }

        Dataset<ClinicalDataModel> clinicalDataModel = ds.as(Encoders.bean(ClinicalDataModel.class));
        List<ClinicalDataModel> clinicalDataModels = clinicalDataModel.collectAsList();
        return clinicalDataModels.stream()
            .map(r -> mapToClinicalData(r)).collect(Collectors.toList());
    }

    @Override
    public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds, String clinicalDataType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ClinicalDataCount> fetchClinicalDataCounts(List<String> studyIds, List<String> ids,
                                                           List<String> attributeIds, String clinicalDataType) {
        List<Dataset<Row>> res = new ArrayList<>();

        Set<String> studyIdSet = new HashSet<>(studyIds);
        String dataFile = ParquetConstants.CLINICAL_PATIENT;
        if (PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE.equalsIgnoreCase(clinicalDataType)) {
            dataFile = ParquetConstants.CLINICAL_SAMPLE;
        }
        Dataset<Row> clinicalData = loadStudyFiles(studyIdSet, dataFile);

        // join with clinical sample data to get sample id information.
        if (PersistenceConstants.PATIENT_CLINICAL_DATA_TYPE.equalsIgnoreCase(clinicalDataType)) {
            Dataset<Row> clinicalSampleData = loadStudyFiles(studyIdSet, ParquetConstants.CLINICAL_SAMPLE);
            clinicalData = clinicalData.join(clinicalSampleData.drop("studyId"), "PATIENT_ID");
        }

        // filter by sample id
        if (!CollectionUtils.isEmpty(ids)) {
            String[] caseArr = ids.stream().toArray(String[]::new);
            if (PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE.equalsIgnoreCase(clinicalDataType)) {
                clinicalData = clinicalData.filter(col("SAMPLE_ID").isin(caseArr));
            } else {
                clinicalData = clinicalData.filter(col("PATIENT_ID").isin(caseArr));
            }
        }

        // if attributes is null return data from all columns
        List<String> attributes = attributeIds;
        if (CollectionUtils.isEmpty(attributeIds)) {
            attributes = Arrays.asList(clinicalData.columns());
        }

        clinicalData.createOrReplaceTempView("clinicalData");
        for (String attributeId : attributes) {
            StringBuilder sb = new StringBuilder();
            if ("SAMPLE_COUNT".equalsIgnoreCase(attributeId)) {
                sb.append("SELECT COUNT(*) AS count, countPatient AS value FROM ");
                sb.append("(SELECT COUNT(*) AS countPatient FROM clinicalData GROUP BY PATIENT_ID) AS SUB ");
                sb.append("GROUP BY countPatient");
            } else {
                sb.append("SELECT COUNT(*) AS count, ");
                sb.append(attributeId + " AS value ");
                sb.append("FROM clinicalData ");
                sb.append(" GROUP BY ");
                sb.append(attributeId);
            }

            String query = sb.toString();
            Dataset<Row> sub = spark.sql(query);

            sub = sub.withColumn("attributeId", lit(attributeId));
            res.add(sub);
        }
        Dataset<Row> ds = res.get(0);
        // union all datasets before calling collectAsList which move all  data into the application's driver process.
        for (Dataset<Row> r : res.subList(1, res.size())) {
            ds = ds.unionByName(r);
        }
        List<Row> dataCounts = ds.collectAsList();
        return dataCounts.stream()
            .map(r -> mapToClinicalDataCount(r)).collect(Collectors.toList());
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
        cd.setStudyId(cdm.getStudyId());
        cd.setAttrId(cdm.getAttrId());
        cd.setAttrValue(cdm.getAttrValue());

        return cd;
    }

    // Loads multiple tables with their schemas merged.
    public Dataset<Row> loadStudyFiles(Set<String> studyIds, String file) {
        List<String> studyIdArr = studyIds.stream()
            .map(s -> PARQUET_DIR + ParquetConstants.STUDIES_DIR + s + "/" + file).collect(Collectors.toList());
        Seq<String> fileSeq = JavaConverters.asScalaBuffer(studyIdArr).toSeq();

        UserDefinedFunction getStudyId = udf((String fullPath) -> {
            String[] paths = fullPath.split("/");
            return paths[paths.length-3];
        }, DataTypes.StringType);

        Dataset<Row> df = spark.read()
            .option("mergeSchema", true)
            .parquet(fileSeq)
            .withColumn("studyId", getStudyId.apply(input_file_name()));
        return df;
    }

    private Dataset<Row> getMutationCount(Set<String> studyIds) {
        Dataset<Row> mutationDf = loadStudyFiles(studyIds, ParquetConstants.DATA_MUTATIONS);

        mutationDf = mutationDf.groupBy("Tumor_Sample_Barcode")
            .agg(countDistinct("Chromosome", "Start_Position", "End_Position",
                "Reference_Allele", "Tumor_Seq_Allele1").alias("attrValue"));
        mutationDf = mutationDf.withColumn("attrValue", mutationDf.col("attrValue").cast("string"))
            .withColumn("attrId", lit(MUTATION_COUNT));
        mutationDf = mutationDf.withColumnRenamed("Tumor_Sample_Barcode", "sampleId");
        return mutationDf;
    }

    private Dataset<Row> getFractionGenomeAltered(Set<String> studyIds) {
        Dataset<Row> copyNumberSeg = loadStudyFiles(studyIds, ParquetConstants.CNA_SEG);

        Dataset<Row> copyNumberSegFiltered =copyNumberSeg
            .filter(abs(col("`seg.mean`")).$greater$eq(0.2)).groupBy("ID")
            .agg(sum(col("`loc.end`").minus(col("`loc.start`"))).alias("filteredSum"));

        copyNumberSeg = copyNumberSeg.groupBy("ID")
            .agg(sum(col("`loc.end`").minus(col("`loc.start`"))).alias("sumVal"));
        copyNumberSeg = copyNumberSeg.join(copyNumberSegFiltered, "ID");
        copyNumberSeg = copyNumberSeg.withColumn("attrValue",
            copyNumberSeg.col("filteredSum")
                .divide(copyNumberSeg.col("sumVal"))
                .cast("string"))
            .withColumn("attrId", lit(FRACTION_GENOME_ALTERED));
        copyNumberSeg = copyNumberSeg.withColumnRenamed("ID", "sampleId");
        copyNumberSeg = copyNumberSeg.drop("filteredSum", "sumVal");
        return copyNumberSeg;
    }
}