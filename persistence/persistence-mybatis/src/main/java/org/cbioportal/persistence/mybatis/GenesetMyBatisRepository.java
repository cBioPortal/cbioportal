package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.cbioportal.model.Gene;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenesetRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GenesetMyBatisRepository implements GenesetRepository {

    @Autowired
    GenesetMapper genesetMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

	@Override
	public List<Geneset> getAllGenesets(String projection, Integer pageSize, Integer pageNumber) {
		
		return genesetMapper.getGenesets(projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), "EXTERNAL_ID", "ASC");
	}

	@Override
	public BaseMeta getMetaGenesets() {

		return genesetMapper.getMetaGenesets();
	}

	@Override
	public Geneset getGeneset(String genesetId) {
		
		return genesetMapper.getGenesetByGenesetId(genesetId, PersistenceConstants.DETAILED_PROJECTION);
	}
	
	@Override
	public List<Geneset> fetchGenesets(List<String> genesetIds) {
		
		return genesetMapper.fetchGenesets(genesetIds);
	}

	@Override
	public List<Gene> getGenesByGenesetId(String genesetId) {
		
		return genesetMapper.getGenesByGenesetId(genesetId, PersistenceConstants.SUMMARY_PROJECTION);
	}
}
