package org.cbioportal.legacy.service.impl;

import java.util.List;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.Geneset;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.GenesetRepository;
import org.cbioportal.legacy.service.GenesetService;
import org.cbioportal.legacy.service.exception.GenesetNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenesetServiceImpl implements GenesetService {

  @Autowired private GenesetRepository genesetRepository;

  @Override
  public List<Geneset> getAllGenesets(String projection, Integer pageSize, Integer pageNumber) {
    List<Geneset> genesetList = genesetRepository.getAllGenesets(projection, pageSize, pageNumber);
    return genesetList;
  }

  @Override
  public BaseMeta getMetaGenesets() {
    return genesetRepository.getMetaGenesets();
  }

  @Override
  public Geneset getGeneset(String genesetId) throws GenesetNotFoundException {
    Geneset geneset = genesetRepository.getGeneset(genesetId);

    if (geneset == null) {
      throw new GenesetNotFoundException(genesetId);
    }
    return geneset;
  }

  @Override
  public List<Gene> getGenesByGenesetId(String genesetId) throws GenesetNotFoundException {

    // validate (throws exception if not found):
    this.getGeneset(genesetId);
    return genesetRepository.getGenesByGenesetId(genesetId);
  }

  @Override
  public List<Geneset> fetchGenesets(List<String> genesetIds) {

    return genesetRepository.fetchGenesets(genesetIds);
  }

  @Override
  public String getGenesetVersion() {
    String genesetVersion = genesetRepository.getGenesetVersion();
    if (genesetVersion == null) {
      return "";
    } else {
      return genesetVersion;
    }
  }
}
