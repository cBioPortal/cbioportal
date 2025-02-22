package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.ReferenceGenomeGene;
import org.springframework.cache.annotation.Cacheable;

public interface ReferenceGenomeGeneRepository {
  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<ReferenceGenomeGene> getAllGenesByGenomeName(String genomeName);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<ReferenceGenomeGene> getGenesByHugoGeneSymbolsAndGenomeName(
      List<String> geneIds, String genomeName);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<ReferenceGenomeGene> getGenesByGenomeName(List<Integer> geneIds, String genomeName);

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  ReferenceGenomeGene getReferenceGenomeGene(Integer geneId, String genomeName);

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  ReferenceGenomeGene getReferenceGenomeGeneByEntityId(Integer geneticEntityId, String genomeName);
}
