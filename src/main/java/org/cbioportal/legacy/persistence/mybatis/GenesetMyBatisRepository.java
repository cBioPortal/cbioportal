package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.Geneset;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.GenesetRepository;
import org.cbioportal.legacy.persistence.PersistenceConstants;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GenesetMyBatisRepository implements GenesetRepository {

  @Autowired GenesetMapper genesetMapper;

  @Override
  public List<Geneset> getAllGenesets(String projection, Integer pageSize, Integer pageNumber) {

    return genesetMapper.getGenesets(
        projection,
        pageSize,
        PaginationCalculator.offset(pageSize, pageNumber),
        "external_id",
        "ASC");
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

  @Override
  public String getGenesetVersion() {

    return genesetMapper.getGenesetVersion();
  }
}
