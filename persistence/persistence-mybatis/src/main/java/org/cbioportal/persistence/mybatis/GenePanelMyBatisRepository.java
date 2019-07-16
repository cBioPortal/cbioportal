package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenePanelRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("genePanelMyBatisRepository")
public class GenePanelMyBatisRepository implements GenePanelRepository {
    
    @Autowired
    private GenePanelMapper genePanelMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<GenePanel> getAllGenePanels(String projection, Integer pageSize, Integer pageNumber, String sortBy, 
                                            String direction) {
        
        return genePanelMapper.getAllGenePanels(projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber),
            sortBy, direction);
    }

    @Override
    public BaseMeta getMetaGenePanels() {
        
        return genePanelMapper.getMetaGenePanels();
    }

    @Override
    public GenePanel getGenePanel(String genePanelId) {
        
        return genePanelMapper.getGenePanel(genePanelId, PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
	public List<GenePanel> fetchGenePanels(List<String> genePanelIds, String projection) {
        
        return genePanelMapper.fetchGenePanels(genePanelIds, projection);
	}

    @Override
    public List<GenePanelData> getGenePanelData(String molecularProfileId, String sampleListId) {
        
        return genePanelMapper.getGenePanelDataBySampleListId(molecularProfileId, sampleListId);
    }

    @Override
    public List<GenePanelData> fetchGenePanelData(String molecularProfileId, List<String> sampleIds) {
        
        return genePanelMapper.getGenePanelDataBySampleIds(molecularProfileId, sampleIds);
    }

    @Override
    public List<GenePanelData> fetchGenePanelDataInMultipleMolecularProfiles(List<String> molecularProfileIds, 
        List<String> sampleIds) {
        
        return genePanelMapper.fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileIds, sampleIds);
	}

    @Override
    public List<GenePanelToGene> getGenesOfPanels(List<String> genePanelIds) {
        
        return genePanelMapper.getGenesOfPanels(genePanelIds);
    }
}
