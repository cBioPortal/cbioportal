package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.cbioportal.persistence.spark.util.ParquetLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


@Component
@Qualifier("discreteCopyNumberSparkRepository")
public class DiscreteCopyNumberSparkRepository implements DiscreteCopyNumberRepository {

    @Autowired
    private SparkSession spark;
    
    @Autowired
    private ParquetLoader parquetLoader;

    @Value("${data.parquet.folder}")
    private String PARQUET_DIR;
    
    @Override
    public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds, List<Integer> alterationTypes, String projection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds, List<Integer> alterationTypes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInMolecularProfile(String molecularProfileId, List<String> sampleIds, List<Integer> entrezGeneIds, List<Integer> alterationTypes, String projection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds, List<Integer> alterationTypes, String projection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta fetchMetaDiscreteCopyNumbersInMolecularProfile(String molecularProfileId, List<String> sampleIds, List<Integer> entrezGeneIds, List<Integer> alterationTypes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(String molecularProfileId, List<String> sampleIds, List<Integer> entrezGeneIds, List<Integer> alterations) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<CopyNumberCountByGene> getSampleCountInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds, List<Integer> alterations) {
        
        // filter by moleclarProfileIds
        Dataset<Row> casels = parquetLoader.loadCaseListFiles(spark, new HashSet<>(molecularProfileIds), false);
        casels = casels.withColumnRenamed("case_list_ids", "SAMPLE_ID");
        if (!CollectionUtils.isEmpty(sampleIds)) {
            casels = casels
                .where(casels.col("SAMPLE_ID").isin(sampleIds.toArray()));
        }
        // get studyIds and filter CNA data by alterations
        List<String> studyIds = casels.select("cancer_study_identifier").distinct()
            .collectAsList().stream().map(r -> r.getString(0)).collect(Collectors.toList());
        Dataset<Row> cnaData = parquetLoader.loadStudyFiles(spark, new HashSet<>(studyIds), "/" + ParquetConstants.DATA_CNA, false);
        if (!CollectionUtils.isEmpty(alterations)) {
            cnaData = cnaData.where(cnaData.col("VALUE").isin(alterations.toArray()));
        }
        // filter by entrezGeneIds
        if (!CollectionUtils.isEmpty(entrezGeneIds)) {
            cnaData = cnaData.where(cnaData.col("Entrez_Gene_Id").isin(entrezGeneIds.toArray()));
        }
        cnaData = cnaData.join(casels, "SAMPLE_ID");

        cnaData.createOrReplaceTempView("cna");
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT Entrez_Gene_Id, Hugo_Symbol, VALUE, COUNT(DISTINCT(SAMPLE_ID)) as numberOfAlteredCases, cytoband ");
        sb.append("FROM cna ");
        sb.append("GROUP BY Entrez_Gene_Id, VALUE, Hugo_Symbol, cytoband");

        return spark.sql(sb.toString()).collectAsList().stream()
            .map(r -> mapToCopyNumberCountByGene(r)).collect(Collectors.toList());
    }

    @Override
    public List<CopyNumberCountByGene> getPatientCountByGeneAndAlterationAndPatientIds(String molecularProfileId, List<String> patientIds, List<Integer> entrezGeneIds, List<Integer> alterations) {

        Dataset<Row> casels = spark.read().parquet(PARQUET_DIR + ParquetConstants.CASE_LIST_DIR + molecularProfileId);
        casels = casels.withColumnRenamed("case_list_ids", "SAMPLE_ID");

        // get studyId
        List<String> studyIds = casels.select("cancer_study_identifier").distinct()
            .collectAsList().stream().map(r -> r.getString(0)).collect(Collectors.toList());
        String studyId = studyIds.get(0);
        // filter by alterations
        Dataset<Row> cnaData = spark.read().parquet(PARQUET_DIR + ParquetConstants.STUDIES_DIR + studyId + "/" + ParquetConstants.DATA_CNA);
        if (!CollectionUtils.isEmpty(alterations)) {
            cnaData = cnaData.where(cnaData.col("VALUE").isin(alterations.toArray()));
        }
        // filter by entrezGeneIds
        if (!CollectionUtils.isEmpty(entrezGeneIds)) {
            cnaData = cnaData.where(cnaData.col("Entrez_Gene_Id").isin(entrezGeneIds.toArray()));
        }
        // filter by patientIds
        Dataset<Row> clinicalSample = spark.read().parquet(PARQUET_DIR + ParquetConstants.STUDIES_DIR + studyId + "/" + ParquetConstants.CLINICAL_SAMPLE);
        if (!CollectionUtils.isEmpty(patientIds)) {
            clinicalSample = clinicalSample
                .where(clinicalSample.col("PATIENT_ID").isin(patientIds.toArray()));
        }
        casels = casels.join(clinicalSample, "SAMPLE_ID");
        cnaData = cnaData.join(casels, "SAMPLE_ID");

        String mainCol = Arrays.asList(cnaData.columns()).contains("Entrez_Gene_Id") ? "Entrez_Gene_Id" : "Hugo_Symbol";
        cnaData.createOrReplaceTempView("cna");
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT Entrez_Gene_Id, Hugo_Symbol, VALUE, COUNT(DISTINCT(PATIENT_ID)) as numberOfAlteredCases, cytoband ");
        sb.append("FROM cna ");
        sb.append("GROUP BY Entrez_Gene_Id, VALUE, Hugo_Symbol, cytoband");
        
        return spark.sql(sb.toString()).collectAsList().stream()
            .map(r -> mapToCopyNumberCountByGene(r)).collect(Collectors.toList());
    }

    private CopyNumberCountByGene mapToCopyNumberCountByGene(Row row) {
        CopyNumberCountByGene cncg = new CopyNumberCountByGene();
        cncg.setEntrezGeneId(Integer.parseInt(row.getString(0)));
        cncg.setHugoGeneSymbol(row.getString(1));
        cncg.setAlteration(Integer.parseInt(row.getString(2)));
        cncg.setNumberOfAlteredCases((int) row.getLong(3));
        cncg.setCytoband(row.getString(4));
        return cncg;
    }
}
