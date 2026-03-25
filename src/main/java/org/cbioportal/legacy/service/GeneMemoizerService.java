package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.ReferenceGenomeGene;

@Deprecated
public interface GeneMemoizerService {
  List<ReferenceGenomeGene> fetchGenes(String genomeName);

  void cacheGenes(List<ReferenceGenomeGene> genes, String genomeName);
}
