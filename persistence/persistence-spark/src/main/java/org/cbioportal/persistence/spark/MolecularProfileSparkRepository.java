package org.cbioportal.persistence.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.cbioportal.persistence.spark.util.LoadParquet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;
import static org.spark_project.guava.base.CaseFormat.*;

@Component
public class MolecularProfileSparkRepository implements MolecularProfileRepository {

    @Autowired
    private OffsetCalculator offsetCalculator;
    
    @Autowired
    private LoadParquet loadParquet;

    private static Log logger = LogFactory.getLog(MolecularProfileSparkRepository.class);

    @Override
    public List<MolecularProfile> getAllMolecularProfiles(String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        return getAllMolecularProfilesInStudies(null, projection, pageSize,
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaMolecularProfiles() {
        return null;
    }

    @Override
    public MolecularProfile getMolecularProfile(String molecularProfileId) {
        return null;
    }

    @Override
    public List<MolecularProfile> getMolecularProfiles(List<String> molecularProfileIds, String projection) {
        return null;
    }

    @Override
    public BaseMeta getMetaMolecularProfiles(List<String> molecularProfileIds) {
        return null;
    }

    @Override
    public List<MolecularProfile> getAllMolecularProfilesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        return getAllMolecularProfilesInStudies(Arrays.asList(studyId), projection, pageSize,
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaMolecularProfilesInStudy(String studyId) {
        return null;
    }

    @Override
    public List<MolecularProfile> getMolecularProfilesInStudies(List<String> studyIds, String projection) {
        return getAllMolecularProfilesInStudies(studyIds, projection, 0, 0, null, null);
    }

    @Override
    public BaseMeta getMetaMolecularProfilesInStudies(List<String> studyIds) {
        return null;
    }

    @Override
    public List<MolecularProfile> getMolecularProfilesReferredBy(String referringMolecularProfileId) {
        return null;
    }

    @Override
    public List<MolecularProfile> getMolecularProfilesReferringTo(String referredMolecularProfileId) {
        return null;
    }

    private Dataset<Row> selectClause(Dataset<Row> df, String prefix, String projection) {
        List<Column> columns = new ArrayList<Column>();
        columns.add(new Column("geneticProfileId"));
        columns.add(new Column("stableId"));
        columns.add(new Column("cancerStudyId"));
        columns.add(new Column("cancerStudyIdentifier"));

        if ("SUMMARY".equalsIgnoreCase(projection)
            || "DETAILED".equalsIgnoreCase(projection)) {
            columns.add(new Column("geneticAlterationType"));
            columns.add(new Column("dataType")); // TODO let's see if the caseinsensitivity works. if not rename.
            columns.add(new Column("name"));
            columns.add(new Column("description"));
            columns.add(new Column("showProfileInAnalysisTab"));
        }
        // StudyMapper select
        if ("DETAILED".equalsIgnoreCase(projection)) {
            columns.add(new Column("cancer_study:cancerStudyId"));
            columns.add(new Column("cancer_study:cancerStudyIdentifier"));
            columns.add(new Column("cancer_study:typeOfCancerId"));
            columns.add(new Column("cancer_study:atudyName"));
            columns.add(new Column("cancer_study:shortName"));
            columns.add(new Column("cancer_study:description"));
            columns.add(new Column("cancer_study:publicStudy"));
            columns.add(new Column("cancer_study:pmid"));
            columns.add(new Column("cancer_study:citation"));
            columns.add(new Column("cancer_study:groups"));
            columns.add(new Column("cancer_study:status"));
            columns.add(new Column("cancer_study:importDate"));
        }
        /*
        genetic_profile.GENETIC_PROFILE_ID AS "${prefix}molecularProfileId",
        genetic_profile.STABLE_ID AS "${prefix}stableId",
        genetic_profile.CANCER_STUDY_ID AS "${prefix}cancerStudyId",
        cancer_study.CANCER_STUDY_IDENTIFIER AS "${prefix}cancerStudyIdentifier"
        <if test="projection == 'SUMMARY' || projection == 'DETAILED'">
            ,
            genetic_profile.GENETIC_ALTERATION_TYPE AS "${prefix}molecularAlterationType",
            genetic_profile.DATATYPE AS "${prefix}datatype",
            genetic_profile.NAME AS "${prefix}name",
            genetic_profile.DESCRIPTION AS "${prefix}description",
            genetic_profile.SHOW_PROFILE_IN_ANALYSIS_TAB AS "${prefix}showProfileInAnalysisTab"
        </if>
        <if test="projection == 'DETAILED'">
            ,
            <include refid="org.cbioportal.persistence.mybatis.StudyMapper.select">
                <property name="prefix" value="${prefix}cancerStudy."/>
            </include>
        </if>
         */
        
        df = df.withColumnRenamed("geneticProfileId", "molecularProfileId")
               .withColumnRenamed("geneticAlterationType", "molecularAlterationType")
               .withColumnRenamed("cancer_study:public", "cancer_study:publicStudy");
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
    
    private MolecularProfile mapToMolecularProfile(Row row, String[] cols, String projection) {
        MolecularProfile molecularProfile = new MolecularProfile();
        List<Field> fields = Arrays.asList(MolecularProfile.class.getDeclaredFields());
        Map<String, Field> fieldMap = fields.stream().collect(Collectors.toMap(f -> f.getName(), f -> f));

        CancerStudy cancerStudy = new CancerStudy();
        List<Field> cancerStudyFields = Arrays.asList(CancerStudy.class.getDeclaredFields());
        Map<String, Field> cancerStudyFieldMap = cancerStudyFields.stream().collect(Collectors.toMap(f -> f.getName(), f -> f));

        Field field = null;
        for (int i = 0; i < cols.length; i++) {
            String colNm = cols[i];

            if ("MolecularAlterationType".equalsIgnoreCase(colNm)) {
                MolecularProfile.MolecularAlterationType mat = 
                    Enum.valueOf(MolecularProfile.MolecularAlterationType.class, (String) row.get(i));
                molecularProfile.setMolecularAlterationType(mat);
                
            } else if (colNm.startsWith("cancer_study:")) {
                if ("DETAILED".equalsIgnoreCase(projection)) {
                    field = cancerStudyFieldMap.get(colNm.substring(13));
                    setField(cancerStudy, field, row, i);
                }
    
            } else {
                field = fieldMap.get(colNm);
                setField(molecularProfile, field, row, i);
            }
        }

        if ("DETAILED".equalsIgnoreCase(projection)) {
            molecularProfile.setCancerStudy(cancerStudy);
        }
        return molecularProfile;
    }
    
    private List<MolecularProfile> getAllMolecularProfilesInStudies(List<String> studyIds, String projection, 
        Integer limit, Integer offset, String sortBy, String direction) {
        
        Dataset<Row> geneProfile = loadParquet.loadDataFile(studyIds.get(0), "genetic_profile");
        Dataset<Row> cancerStudy = loadParquet.loadDataFile(studyIds.get(0), "cancer_study");
        
        geneProfile = renameCols(geneProfile, null);
        cancerStudy = renameCols(cancerStudy, "cancer_study");
        
        Dataset<Row> from = geneProfile.join(cancerStudy, 
            geneProfile.col("cancerStudyId").equalTo(cancerStudy.col("cancer_study:cancerStudyId")));
        Dataset<Row> df = selectClause(from, "", projection);
        
        if (studyIds != null && !studyIds.isEmpty()) {
            String[] studyArr = studyIds.stream().toArray(String[]::new);
            df = df.where(col("cancerStudyIdentifier").isin(studyArr));
        }
        df = orderByLimitOffset(df, sortBy, direction, projection, limit, offset);

        String[] cols = df.columns();
        List<Row> rowls = df.collectAsList();
        List<MolecularProfile> molecularProfiles = rowls.stream()
            .map(r -> mapToMolecularProfile(r, cols, projection)).collect(Collectors.toList());

        return molecularProfiles;
    }
    
    private Dataset<Row> orderByLimitOffset(Dataset<Row> df, String sortBy, String direction, String projection,
                                            Integer limit, Integer offset) {
        // Default sort by primary key
        if (sortBy == null) {
            df = df.orderBy("geneticProfileId");
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
}
