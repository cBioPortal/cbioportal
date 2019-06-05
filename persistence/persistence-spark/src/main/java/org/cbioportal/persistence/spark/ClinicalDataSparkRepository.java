package org.cbioportal.persistence.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.*;
import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.cbioportal.persistence.spark.util.LoadParquet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;


@Component
@Qualifier("clinicalDataSparkRepository")
public class ClinicalDataSparkRepository implements ClinicalDataRepository {

    @Autowired
    LoadParquet loadParquet;

    @Autowired
    SparkSession spark;
    
    @Autowired
    private OffsetCalculator offsetCalculator;

    private static Log logger = LogFactory.getLog(ClinicalDataSparkRepository.class);

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
        List<ClinicalData> res = new ArrayList<>();
        for (String studyId : studyIds) {
            Dataset<Row> clinicalData = loadParquet
                .loadDataFile(studyId, "data_clinical_"+ clinicalDataType.toLowerCase() + ".txt");
    
            if (PersistenceConstants.PATIENT_CLINICAL_DATA_TYPE.equalsIgnoreCase(clinicalDataType)) {
                Dataset<Row> clinicalSampleData = loadParquet
                    .loadDataFile(studyId, "data_clinical_sample.txt");
                clinicalData = clinicalData.join(clinicalSampleData, "PATIENT_ID");
            }
    
            // filter by sample id
            if (sampleIds != null) {
                String[] sampleArr = sampleIds.stream().toArray(String[]::new);
                clinicalData = clinicalData.filter(col("SAMPLE_ID").isin(sampleArr));
            }

            // if attributes is null return data from all columns
            List<String> attributes = attributeIds;
            if (attributeIds == null) {
                attributes = Arrays.asList(clinicalData.columns());
            }

            clinicalData.createOrReplaceTempView("clinicalData");

            for (String c : attributes) {
                StringBuilder sb = new StringBuilder();
                sb.append("SELECT clinicalData.PATIENT_ID, clinicalData.SAMPLE_ID, ");
                sb.append("clinicalData." + c + " ");
                sb.append("FROM clinicalData ");
    
                String query = sb.toString();
                Dataset<Row> sub = spark.sql(query);
    
                sub = sub.withColumn("attributeId", lit(c))
                        .withColumn("studyId", lit(studyId));
                sub.show();
    
                List<Row> subls = sub.collectAsList();
                List<ClinicalData> dataCount = subls.stream()
                    .map(r -> mapToClinicalData(r)).collect(Collectors.toList());
                
                res.addAll(dataCount);
            }

        }
        return res;
        
    }

    @Override
    public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds, String clinicalDataType) {
        throw new UnsupportedOperationException();
    }

    // Appending Datasets converted to Lists is Faster then unionByName on datasets.
    @Override
    public List<ClinicalDataCount> fetchClinicalDataCounts(List<String> studyIds, List<String> sampleIds,
                                      List<String> attributeIds, String clinicalDataType) {

        Dataset<Row> clinicalData = loadParquet
            .loadDataFiles(studyIds, "data_clinical_"+ clinicalDataType.toLowerCase() + ".txt");
        
        if (PersistenceConstants.PATIENT_CLINICAL_DATA_TYPE.equalsIgnoreCase(clinicalDataType)) {
            Dataset<Row> clinicalSampleData = loadParquet
                .loadDataFiles(studyIds, "data_clinical_sample.txt");
            clinicalData = clinicalData.join(clinicalSampleData, "PATIENT_ID");
        }

        // filter by sample id
        if (sampleIds != null) {
            String[] sampleArr = sampleIds.stream().toArray(String[]::new);
            clinicalData = clinicalData.filter(col("SAMPLE_ID").isin(sampleArr));
        }
        
        clinicalData.createOrReplaceTempView("clinicalData");
        
        List<ClinicalDataCount> res = new ArrayList<>();
        // if attributes is null return data from all columns
        List<String> attributes = attributeIds;
        if (attributeIds == null) {
            attributes = Arrays.asList(clinicalData.columns());
        }
        
        for (String c : attributes) {
            StringBuilder sb = new StringBuilder();
            if ("SAMPLE_COUNT".equalsIgnoreCase(c)) {
                sb.append("SELECT COUNT(*) AS count, countPatient AS value FROM ");
                sb.append("(SELECT COUNT(*) AS countPatient FROM clinicalData GROUP BY clinicalData.PATIENT_ID) AS SUB ");
                sb.append("GROUP BY countPatient");
            } else {
                sb.append("SELECT COUNT(*) AS count, ");
                sb.append("clinicalData." + c + " AS value ");
                sb.append("FROM clinicalData ");
                sb.append("GROUP BY ");
                sb.append("clinicalData." + c);
            }
            
            String query = sb.toString();
            Dataset<Row> sub = spark.sql(query);

            sub = sub.withColumn("attributeId", lit(c));
            // sub.show();
            
            List<Row> subls = sub.collectAsList();
            List<ClinicalDataCount> dataCount = subls.stream()
                .map(r -> mapToClinicalDataCount(r)).collect(Collectors.toList());

            res.addAll(dataCount);
        }

        return res;
    }
    
    private ClinicalDataCount mapToClinicalDataCount(Row row) {
        ClinicalDataCount cdc = new ClinicalDataCount();
        cdc.setCount((int) row.getLong(0));
        cdc.setValue(String.valueOf(row.get(1)));
        cdc.setAttributeId(String.valueOf(row.get(2)));

        return cdc;
    }
    
    private ClinicalData mapToClinicalData(Row row) {
        ClinicalData cd = new ClinicalData();
        cd.setPatientId(row.getString(0));
        return cd;
    }
}
