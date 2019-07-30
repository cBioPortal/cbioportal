package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataTypes;
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
import org.springframework.util.CollectionUtils;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;

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
        Dataset<Row> casels =  loadFiles(new HashSet<>(molecularProfileIds));
        if (!CollectionUtils.isEmpty(sampleIds)) {
            casels = casels.where(casels.col("case_list_ids").isin(sampleIds.toArray()));
        }
        Dataset<Row> result = casels.select("case_list_ids", "molecularProfileId");
            
        return result.collectAsList().stream().
            map(r -> mapToGenePanelData(r)).collect(Collectors.toList());
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

    // Loads multiple tables with their schemas merged.
    private Dataset<Row> loadFiles(Set<String> molecularProfileIds) {
        List<String> molecularProfileArr = molecularProfileIds.stream()
            .map(id -> PARQUET_DIR + ParquetConstants.CASE_LIST_DIR + id).collect(Collectors.toList());
        Seq<String> fileSeq = JavaConverters.asScalaBuffer(molecularProfileArr).toSeq();

        UserDefinedFunction getStudyId = udf((String fullPath) -> {
            String[] paths = fullPath.split("/");
            return paths[paths.length-2];
        }, DataTypes.StringType);

        return spark.read()
            .option("mergeSchema", true)
            .parquet(fileSeq)
            .withColumn("molecularProfileId", getStudyId.apply(input_file_name()));
    }
}
