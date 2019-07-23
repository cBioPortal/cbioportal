package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
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
        for (String molecularProfileId: new HashSet<>(molecularProfileIds)) {
            Dataset<Row> casels = spark.read()
                .parquet(PARQUET_DIR + ParquetConstants.CASE_LIST_DIR + molecularProfileId);

            if (sampleIds != null && !sampleIds.isEmpty()) {
                casels = casels
                    .where(casels.col("case_list_ids").isin(sampleIds.toArray()));
            }
            
            Dataset<Row> sub = casels.select("case_list_ids");
            sub = sub.withColumn("molecularProfileId", lit(molecularProfileId));
            res.add(sub);
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
        gpd.setMolecularProfileId(row.getString(1));
        return gpd;
    }
}
