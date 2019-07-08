package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.lit;

@Component
@Qualifier("discreteCopyNumberSparkRepository")
public class DiscreteCopyNumberSparkRepository implements DiscreteCopyNumberRepository {

    @Autowired
    private SparkSession spark;

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
        throw new UnsupportedOperationException();

    }
    
    
    public List<CopyNumberCountByGene> getSampleCountInMultipleMolecularProfilesSpark(List<String> studyIds, List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds, List<Integer> alterations) {
        
        for (String studyId : studyIds) {
            Dataset<Row> fusionData = spark.read()
                .parquet(PARQUET_DIR + "/" + studyId + "/data_fusions.txt.parquet");
            Dataset<Row> cnaData = spark.read()
                .parquet(PARQUET_DIR + "/" + studyId + "/data_CNA.txt.parquet");

            fusionData = fusionData.join(cnaData, "Hugo_Symbol");
            fusionData.createOrReplaceTempView("fusion");
            
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT Hugo_Symbol as hugoGeneSymbol, ");
            sb.append("Entrez_Gene_Id as entrezGeneId ");
            //alteration from data_CNA
            //sb.append("(SELECT " + cnaData.col() + ") as aliteration ");
            sb.append("FROM fusion ");
            
            Dataset<Row> sub = spark.sql(sb.toString());
            sub = sub.withColumn("alteration", lit(1));
        }
        return null;
    }

    @Override
    public List<CopyNumberCountByGene> getPatientCountByGeneAndAlterationAndPatientIds(String molecularProfileId, List<String> patientIds, List<Integer> entrezGeneIds, List<Integer> alterations) {
        throw new UnsupportedOperationException();
    }
}
