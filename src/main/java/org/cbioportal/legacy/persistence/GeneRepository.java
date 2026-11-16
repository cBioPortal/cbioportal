/*
 * Copyright (c) 2016 - 2019 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.cbioportal.legacy.persistence;

import java.util.List;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.GeneAlias;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.springframework.cache.annotation.Cacheable;

public interface GeneRepository {

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<Gene> getAllGenes(
      String keyword,
      String alias,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta getMetaGenes(String keyword, String alias);

  Gene getGeneByGeneticEntityId(Integer geneticEntityId);

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  Gene getGeneByEntrezGeneId(Integer entrezGeneId);

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  Gene getGeneByHugoGeneSymbol(String hugoGeneSymbol);

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<String> getAliasesOfGeneByEntrezGeneId(Integer entrezGeneId);

  @Cacheable(
      cacheResolver = "staticRepositoryCacheOneResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<String> getAliasesOfGeneByHugoGeneSymbol(String hugoGeneSymbol);

  // not cached because this is called only a single time, during @PostConstruct method of
  // GeneServiceImpl
  List<GeneAlias> getAllAliases();

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<Gene> fetchGenesByEntrezGeneIds(List<Integer> entrezGeneIds, String projection);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  List<Gene> fetchGenesByHugoGeneSymbols(List<String> hugoGeneSymbols, String projection);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta fetchMetaGenesByEntrezGeneIds(List<Integer> entrezGeneIds);

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()")
  BaseMeta fetchMetaGenesByHugoGeneSymbols(List<String> hugoGeneSymbols);
}
