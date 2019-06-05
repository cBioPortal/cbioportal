package org.cbioportal.persistence.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.*;
import static org.apache.spark.sql.functions.*;
import static org.spark_project.guava.base.CaseFormat.LOWER_CAMEL;
import static org.spark_project.guava.base.CaseFormat.LOWER_UNDERSCORE;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.Patient;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.cbioportal.persistence.spark.util.LoadParquet;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.collection.JavaConversions;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class SampleSparkRepository implements SampleRepository {

    @Autowired
    LoadParquet loadParquet;

    @Autowired
    private OffsetCalculator offsetCalculator;

    private static Log logger = LogFactory.getLog(SampleSparkRepository.class);
    
    @Override
    public List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
                                             String sortBy, String direction) {
        return getSamples(Arrays.asList(studyId), null, null, projection, pageSize,
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSamplesInStudy(String studyId) {
        return null;
    }

    @Override
    public List<Sample> getAllSamplesInStudies(List<String> studyIds, String projection, Integer pageSize,
                                               Integer pageNumber, String sortBy, String direction) {
        return getSamples(studyIds, null, null, projection, pageSize,
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public Sample getSampleInStudy(String studyId, String sampleId) {
        return null;
    }

    @Override
    public List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection, Integer pageSize,
                                                      Integer pageNumber, String sortBy, String direction) {
        return getSamples(Arrays.asList(studyId), patientId, null, projection, pageSize,
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId) {
        return null;
    }

    @Override
    public List<Sample> getAllSamplesOfPatientsInStudy(String studyId, List<String> patientIds, String projection) {
        return null;
    }

    @Override
    public List<Sample> getSamplesOfPatientsInMultipleStudies(List<String> studyIds, List<String> patientIds, String projection) {
        return null;
    }

    @Override
    public List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection) {
        return getSamples(studyIds, null, sampleIds, projection, 0, 0, null, null);
    }
    
    @Override
    public List<Sample> fetchSamples(List<String> sampleListIds, String projection) {
        return null;
    }

    @Override
    public BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds) {
        return null;
    }

    @Override
    public BaseMeta fetchMetaSamples(List<String> sampleListIds) {
        return null;
    }

    @Override
    public List<Sample> getSamplesByInternalIds(List<Integer> internalIds) {
        return null;
    }
    
    private Dataset<Row> selectClause(Dataset<Row> df, String projection) {

        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("internalId"));
        columns.add(new Column("stableId"));
        columns.add(new Column("patient:stableId").as("patientStableId"));
        columns.add(new Column("cancer_study:cancerStudyIdentifier").as("cancerStudyIdentifier"));
        
        if ("SUMMARY".equalsIgnoreCase(projection) 
            || "DETAILED".equalsIgnoreCase(projection)) {
            columns.add(new Column("sampleType"));
            columns.add(new Column("patientId"));
        }
        // PatientMapper, StudyMapper select
        if ("DETAILED".equalsIgnoreCase(projection)) {
            columns.add(new Column("patient:internalId"));
            columns.add(new Column("patient:stableId"));
            columns.add(new Column("patient:cancerStudyId"));
            columns.add(new Column("cancer_study:cancerStudyIdentifier").as("patient:cancerStudyIdentifier"));
            
            columns.add(new Column("cancer_study:cancerStudyId"));
            columns.add(new Column("cancer_study:cancerStudyIdentifier"));
            columns.add(new Column("cancer_study:typeOfCancerId"));
            columns.add(new Column("cancer_study:name"));
            columns.add(new Column("cancer_study:shortName"));
            columns.add(new Column("cancer_study:description"));
            columns.add(new Column("cancer_study:publicStudy"));
            columns.add(new Column("cancer_study:pmid"));
            columns.add(new Column("cancer_study:citation"));
            columns.add(new Column("cancer_study:groups"));
            columns.add(new Column("cancer_study:status"));
            columns.add(new Column("cancer_study:importDate"));
        }

        df = df.withColumnRenamed("cancer_study:public", "cancer_study:publicStudy");
        df = df.select(JavaConversions.asScalaBuffer(columns).toSeq());
        return df;    
    }

    private Dataset<Row> fromClause(List<String> studyIds) {
        
        Dataset<Row> sample = loadParquet.loadDataFile(studyIds.get(0),"sample");
        Dataset<Row> patient = loadParquet.loadDataFile(studyIds.get(0),"patient");
        Dataset<Row> cancerStudy = loadParquet.loadDataFile(studyIds.get(0),"cancer_study");

        sample = renameCols(sample, null);
        patient = renameCols(patient, "patient");
        cancerStudy = renameCols(cancerStudy, "cancer_study");
        
        return sample
            .join(patient, sample.col("patientId").equalTo(patient.col("patient:internalId")))
            .join(cancerStudy, 
                patient.col("patient:cancerStudyId").equalTo(cancerStudy.col("cancer_study:cancerStudyId")));
    }
    
    private Dataset<Row> whereClause(Dataset<Row> df, String[] studyIds, String[] sampleIds, String patientId) {

        if (sampleIds != null && sampleIds.length > 0) {
            if (studyIds != null && studyIds.length > 0) { 
                if (new HashSet<>(Arrays.asList(studyIds)).size() == 1) {
                    df = df.where(col("cancer_study:cancerStudyIdentifier").equalTo(studyIds[0]));
                    df = df.where(col("stableId").isin(sampleIds));
                } else {
                    df = df.where(col("cancer_study:cancerStudyIdentifier").isin(studyIds))
                        .where(col("stableId").isin(sampleIds));
                }
            }
        } else {
            df = df.where(col("cancer_study:cancerStudyIdentifier").isin(studyIds));
        }
        if (patientId != null) {
            df = df.where(col("patient:stableId").equalTo(patientId));
        }
        return df;
    }

    // Rename columns: column_name -> tablename:columnName
    private static Dataset<Row> renameCols(Dataset<Row> df, String tablename) {
        for (String column : df.columns()) {
            if (tablename == null) {
                df = df.withColumnRenamed(column, LOWER_UNDERSCORE.to(LOWER_CAMEL, column));
            } else {
                df = df.withColumnRenamed(column, tablename + ":" + LOWER_UNDERSCORE.to(LOWER_CAMEL, column));
            }
        }
        return df;
    }
    
    private Dataset<Row> orderByLimitOffset(Dataset<Row> df, String sortBy, String direction, String projection,
                                            Integer limit, Integer offset) {
        // Default sort by primary key
        if (sortBy == null) {
            df = df.orderBy("internalId");
        }
        if (sortBy != null && !"ID".equalsIgnoreCase(projection)) {
            if ("ASC".equalsIgnoreCase(direction)) {
                df = df.orderBy(asc(sortBy));
            } else if ("DESC".equalsIgnoreCase(direction)) {
                df = df.orderBy(desc(sortBy));
            }
        }
        if ("ID".equalsIgnoreCase(projection)) {
            df = df.orderBy(asc("stableId"));
        }
        // Limit & Offset : TODO offset for pagination not supported yet
        if (limit != null && limit != 0) {
            df = df.limit(limit);
        }
        return df;
    }
    
    private void setField(Object toSet, Field field, Row row, int i) {
        Object obj = field.getType().cast(row.get(i));
        try {
            field.setAccessible(true);
            field.set(toSet, obj);
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        }
    }
    
    private Sample mapToSample(Row row, String[] cols, String projection) {
        Sample sample = new Sample();
        List<Field> fields = Arrays.asList(Sample.class.getDeclaredFields());
        Map<String, Field> fieldMap = fields.stream().collect(Collectors.toMap(f -> f.getName(), f -> f));
        
        Patient patient = new Patient();
        List<Field> patientFields = Arrays.asList(Patient.class.getDeclaredFields());
        Map<String, Field> patientFieldMap = patientFields.stream().collect(Collectors.toMap(f -> f.getName(), f -> f));

        CancerStudy cancerStudy = new CancerStudy();
        List<Field> cancerStudyFields = Arrays.asList(CancerStudy.class.getDeclaredFields());
        Map<String, Field> cancerStudyFieldMap = cancerStudyFields.stream().collect(Collectors.toMap(f -> f.getName(), f -> f));
        
        Field field = null;
        
        for (int i = 0; i < cols.length; i++) {
            String colNm = cols[i];
            
            if (colNm.equalsIgnoreCase("SampleType")) {
                Sample.SampleType sampleType = Sample.SampleType.fromString((String) row.get(i));
                sample.setSampleType(sampleType);
                
            } else if (colNm.startsWith("patient:")) {
                if ("DETAILED".equalsIgnoreCase(projection)) {
                    field = patientFieldMap.get(colNm.substring(8));
                    setField(patient, field, row, i);
                }
                
            } else if (colNm.startsWith("cancer_study:")) {
                if ("DETAILED".equalsIgnoreCase(projection)) {
                    field = cancerStudyFieldMap.get(colNm.substring(13));
                    setField(cancerStudy, field, row, i);
                }
                
            } else {
                field = fieldMap.get(colNm);
                setField(sample, field, row, i);
            }
        }

        if ("DETAILED".equalsIgnoreCase(projection)) {
            patient.setCancerStudy(cancerStudy);
            sample.setPatient(patient);
        }

        return sample;
    }
    
    private List<Sample> getSamples(List<String> studyIds, String patientId, List<String> sampleIds, String projection,
            Integer limit, Integer offset, String sortBy, String direction) {
        Dataset<Row> from = fromClause(studyIds);
        Dataset<Row> selected = selectClause(from, projection);

        String[] studyArr = null;
        String[] sampleArr = null;
        if (studyIds != null) {
            studyArr = studyIds.stream().toArray(String[]::new);
        }
        if (sampleIds != null) {
            sampleArr = sampleIds.stream().toArray(String[]::new);
        }
        Dataset<Row> filtered = whereClause(selected, studyArr, sampleArr, patientId);
        filtered = orderByLimitOffset(filtered, sortBy, direction, projection, limit, offset);
        
        String[] cols = filtered.columns();
        List<Row> rowls = filtered.collectAsList();
        List<Sample> samples = rowls.stream()
                .map(r -> mapToSample(r, cols, projection)).collect(Collectors.toList());
        
        return samples;
    }

}
