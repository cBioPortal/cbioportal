package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.spark.util.ParquetLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.persistence.spark.util.ParquetLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("genePanelSparkRepository")
public class GenePanelSparkRepository implements GenePanelRepository {

    @Autowired
    private SparkSession spark;

    @Autowired
    private ParquetLoader parquetLoader;

    @Autowired
    private GeneRepository geneRepository;

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
        Dataset<Row> genePanelData = parquetLoader.loadGenePanelFiles(spark, genePanelIds);
        genePanelData.createOrReplaceTempView("genePanel");

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT stable_id ");
        if (!PersistenceConstants.ID_PROJECTION.equalsIgnoreCase(projection)) {
            sb.append(", description ");
        }
        sb.append("FROM genePanel ");
        sb.append("GROUP BY stable_id");
        if (!PersistenceConstants.ID_PROJECTION.equalsIgnoreCase(projection)) {
            sb.append(", description ");
        }
        sb.append("ORDER BY stable_id");

        return spark.sql(sb.toString()).collectAsList().stream()
            .map(r -> mapToGenePanel(r, projection)).collect(Collectors.toList());
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
        Dataset<Row> casels = parquetLoader.loadCaseListFiles(spark, new HashSet<>(molecularProfileIds), true);
        if (!CollectionUtils.isEmpty(sampleIds)) {
            casels = casels.where(casels.col("case_list_ids").isin(sampleIds.toArray()));
        }
        Dataset<Row> result = casels.select("case_list_ids", "molecularProfileId");

        return result.collectAsList().stream().
            map(r -> mapToGenePanelData(r)).collect(Collectors.toList());
    }

    @Override
    public List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds) {
        Dataset<Row> genePanelData = parquetLoader.loadGenePanelFiles(spark, genePanelIds);

        genePanelData = genePanelData.select("stable_id", "gene_list")
            .orderBy("stable_id", "gene_list");

        List<GenePanelToGene> genePanelToGenes = genePanelData.collectAsList().stream()
            .map(r -> mapToGenePanelToGene(r)).collect(Collectors.toList());

        return getGeneDetails(genePanelToGenes);
    }

    private GenePanelData mapToGenePanelData(Row row) {
        GenePanelData gpd = new GenePanelData();
        gpd.setSampleId(row.getString(0));
        gpd.setMolecularProfileId(row.getString(1));
        return gpd;
    }

    private GenePanel mapToGenePanel(Row row, String projection) {
        GenePanel gp = new GenePanel();
        gp.setStableId(row.getString(0));
        if (!PersistenceConstants.ID_PROJECTION.equalsIgnoreCase(projection)) {
            gp.setDescription(row.getString(1));
        }
        return gp;
    }

    private GenePanelToGene mapToGenePanelToGene(Row row) {
        GenePanelToGene gptg = new GenePanelToGene();
        gptg.setGenePanelId(row.getString(0));
        gptg.setHugoGeneSymbol(row.getString(1));
        return gptg;
    }

    private List<GenePanelToGene> getGeneDetails(List<GenePanelToGene> genePanelToGenes) {
        List<String> hugoGeneSymbols = genePanelToGenes.stream()
            .map(GenePanelToGene::getHugoGeneSymbol).collect(Collectors.toList());
        Map<String, Gene> geneMap = geneRepository.fetchGenesByHugoGeneSymbols(hugoGeneSymbols, PersistenceConstants.SUMMARY_PROJECTION)
            .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Function.identity()));

        List<GenePanelToGene> tobeRemoved = new ArrayList<>();
        for (GenePanelToGene gptg : genePanelToGenes) {
            Gene gene = geneMap.get(gptg.getHugoGeneSymbol());
            if (gene != null) {
                gptg.setEntrezGeneId(gene.getEntrezGeneId());
            } else {
                tobeRemoved.add(gptg);
            }
        }
        genePanelToGenes.removeAll(tobeRemoved);

        return genePanelToGenes;
    }
}