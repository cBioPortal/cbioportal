package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.lit;

@Component
@Qualifier("genePanelSparkRepository")
public class GenePanelSparkRepository implements GenePanelRepository {

    @Autowired
    private SparkSession spark;

    @Value("${data.parquet.folder}")
    private String PARQUET_DIR;
    
    @Autowired
    @Qualifier("molecularProfileMyBatisRepository")
    private MolecularProfileRepository molecularProfileRepository;
    
    @Override
    public List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BaseMeta getMetaGenePanels() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GenePanel getGenePanel(String genePanelId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<GenePanelData> getGenePanelData(String molecularProfileId, String sampleListId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds) {
        
        List<Dataset<Row>> res = new ArrayList<>();
        List<MolecularProfile> molecularProfiles = molecularProfileRepository.getMolecularProfiles(molecularProfileIds, "ID");
        Set<String> studyIds = molecularProfiles.stream().map(mp -> mp.getCancerStudyIdentifier())
            .collect(Collectors.toSet());
        Set<String> molecularProfileSet = new HashSet<>(molecularProfileIds);
        
        for (String studyId: studyIds) {
            Dataset<Row> geneMatrix = spark.read()
                .parquet(PARQUET_DIR + "/" + studyId + "/" + ParquetConstants.GENE_MATRIX);

            if (sampleIds != null && !sampleIds.isEmpty()) {
                geneMatrix = geneMatrix
                    .where(geneMatrix.col("SAMPLE_ID").isin(sampleIds.toArray()));
            }

            List<String> mpIds = molecularProfileSet.stream().filter(id -> id.startsWith(studyId)).collect(Collectors.toList());
            for (String molecularProfileId : mpIds) {
                String colName = molecularProfileId.replaceFirst(studyId + "_", "");
                Dataset<Row> sub = geneMatrix.select("SAMPLE_ID", colName);
                sub = sub.withColumn("molecularProfileId", lit(molecularProfileId));
                res.add(sub);
            }
        }
        Dataset<Row> ds = res.get(0);
        if (res.size() > 1) {
            for (Dataset<Row> sub: res.subList(1, res.size())) {
                ds = ds.unionByName(sub);
            }
        }

        List<Row> resls = ds.collectAsList();
        List<GenePanelData> genePanelData = resls.stream().
            map(r -> mapToGenePanelData(r)).collect(Collectors.toList());
        return genePanelData;
    }

    @Override
    public List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds) {
        throw new UnsupportedOperationException();
    }
    
    private GenePanelData mapToGenePanelData(Row row) {
        GenePanelData gpd = new GenePanelData();
        gpd.setSampleId(row.getString(0));
        gpd.setGenePanelId(row.getString(1));
        gpd.setMolecularProfileId(row.getString(2));
        return gpd;
    }
}
