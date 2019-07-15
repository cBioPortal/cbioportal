package org.cbioportal.persistence.spark;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

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
        // get studyIds..
        Set<String> molecularProfileSet = new TreeSet<>(molecularProfileIds);
        String[] molecularProfiles = (String[]) molecularProfileSet.toArray();
        List<String> studyIds = new ArrayList<>();
        String prefix = StringUtils.getCommonPrefix(molecularProfiles);
        if (prefix != "") {
            // 1 study ids
            if (prefix.endsWith("_")) {
                prefix = prefix.substring(0, prefix.length()-1);
            }
        } else {
            // multiple study ids
        }
        for (String molecularProfileId : new HashSet<>(molecularProfileIds)) {
            
            
        }
        return null;
    }

    @Override
    public List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds) {
        throw new UnsupportedOperationException();
    }
}
