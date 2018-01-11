package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface GeneMapper {

    List<Gene> getGenes(String alias, String projection, Integer limit, Integer offset, String sortBy,
                        String direction);

    BaseMeta getMetaGenes(String alias);

    Gene getGeneByEntrezGeneId(Integer entrezGeneId, String projection);

    Gene getGeneByHugoGeneSymbol(String hugoGeneSymbol, String projection);

    List<String> getAliasesOfGeneByEntrezGeneId(Integer entrezGeneId);

    List<String> getAliasesOfGeneByHugoGeneSymbol(String hugoGeneSymbol);

    List<Gene> getGenesByEntrezGeneIds(List<Integer> entrezGeneIds, String projection);

    List<Gene> getGenesByHugoGeneSymbols(List<String> hugoGeneSymbols, String projection);

    BaseMeta getMetaGenesByEntrezGeneIds(List<Integer> entrezGeneIds);

    BaseMeta getMetaGenesByHugoGeneSymbols(List<String> hugoGeneSymbols);
}
