package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.GeneAlias;
import org.cbioportal.legacy.model.meta.BaseMeta;

public interface GeneMapper {

  List<Gene> getGenes(
      String keyword,
      String alias,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  BaseMeta getMetaGenes(String keyword, String alias);

  Gene getGeneByGeneticEntityId(Integer geneticEntityId, String projection);

  Gene getGeneByEntrezGeneId(Integer entrezGeneId, String projection);

  Gene getGeneByHugoGeneSymbol(String hugoGeneSymbol, String projection);

  List<String> getAliasesOfGeneByEntrezGeneId(Integer entrezGeneId);

  List<String> getAliasesOfGeneByHugoGeneSymbol(String hugoGeneSymbol);

  List<GeneAlias> getAllAliases();

  List<Gene> getGenesByEntrezGeneIds(List<Integer> entrezGeneIds, String projection);

  List<Gene> getGenesByHugoGeneSymbols(List<String> hugoGeneSymbols, String projection);

  BaseMeta getMetaGenesByEntrezGeneIds(List<Integer> entrezGeneIds);

  BaseMeta getMetaGenesByHugoGeneSymbols(List<String> hugoGeneSymbols);
}
