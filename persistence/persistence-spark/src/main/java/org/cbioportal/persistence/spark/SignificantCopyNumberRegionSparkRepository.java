package org.cbioportal.persistence.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.SignificantCopyNumberRegionRepository;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.lit;

@Component
@Qualifier("significantCopyNumberRegionSparkRepository")
public class SignificantCopyNumberRegionSparkRepository implements SignificantCopyNumberRegionRepository {

    @Autowired
    private SparkSession spark;

    @Value("${data.parquet.folder}")
    private String PARQUET_DIR;
    
    @Autowired
    private GeneRepository geneRepository;

    @Override
    public List<Gistic> getSignificantCopyNumberRegions(String studyId, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        String filePath = PARQUET_DIR + ParquetConstants.STUDIES_DIR + studyId + "/" + ParquetConstants.GISTIC_GENES;
        File file = new File(filePath);
        if (file.exists()) {
            Dataset<Row> gisticData = spark.read().parquet(filePath);
            gisticData.createOrReplaceTempView("gistic");
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT chromosome, cytoband, amp, q_value, peak_start, peak_end, genes_in_region ");
            sb.append("FROM gistic ");
            if (sortBy != null && direction != null) {
                sb.append("sort by " + sortBy + " " + direction);
            }
            if (pageNumber != null && pageSize != null) {
                sb.append("LIMIT " + pageSize + " OFFSET " + (pageSize * pageNumber));
            }
            Dataset<Row> result = spark.sql(sb.toString());
            result = result.withColumn("studyId", lit(studyId));

            List<Row> genesInRegion = gisticData.select("genes_in_region").collectAsList();
            Set<String> hugoSymbolSet = new HashSet<>();
            for (Row row : genesInRegion) {
                String[] hugoSymbols = row.getString(0).split(",");
                hugoSymbolSet.addAll(Arrays.asList(hugoSymbols));
            }
            
            if (!PersistenceConstants.ID_PROJECTION.equalsIgnoreCase(projection)) {
                Map<String, Gene> geneMap = geneRepository.fetchGenesByHugoGeneSymbols(new ArrayList<>(hugoSymbolSet), PersistenceConstants.SUMMARY_PROJECTION)
                    .stream().collect(Collectors.toMap(Gene::getHugoGeneSymbol, Function.identity()));
                return result.collectAsList().stream()
                    .map(r -> mapToGistic(r, geneMap)).collect(Collectors.toList());
            } else {
                return result.collectAsList().stream()
                    .map(r -> mapToGistic(r, null)).collect(Collectors.toList());
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public BaseMeta getMetaSignificantCopyNumberRegions(String studyId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<GisticToGene> getGenesOfRegions(List<Long> gisticRoiIds) {
        throw new UnsupportedOperationException();
    }
    
    private Gistic mapToGistic(Row row, Map<String, Gene> geneMap) {
        Gistic gistic = new Gistic();
        gistic.setChromosome(Integer.parseInt(row.getString(0)));
        gistic.setCytoband(row.getString(1));
        gistic.setAmp("1".equalsIgnoreCase(row.getString(2)) ?  true : false);
        gistic.setqValue(new BigDecimal(row.getString(3)));
        gistic.setWidePeakStart(Integer.parseInt(row.getString(4)));
        gistic.setWidePeakEnd(Integer.parseInt(row.getString(5)));
        
        if (geneMap != null) {
            String[] hugoSymbols = row.getString(6).split(",");
            List<GisticToGene> gisticToGenes = new ArrayList<>();
            for (String hugoSymbol : hugoSymbols) {
                Gene gene = geneMap.get(hugoSymbol);
                if (gene != null) {
                    GisticToGene gtg = new GisticToGene();
                    gtg.setEntrezGeneId(gene.getEntrezGeneId());
                    gtg.setHugoGeneSymbol(hugoSymbol);
                    gisticToGenes.add(gtg);
                }
            }
            gistic.setGenes(gisticToGenes);
        }
        return gistic;
    }
}
