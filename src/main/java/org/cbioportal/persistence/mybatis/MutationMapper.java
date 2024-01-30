package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.meta.MutationMeta;

import java.util.List;

public interface MutationMapper {

    List<Mutation> getMutationsBySampleListId(String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds,
                                              boolean snpOnly, String projection, Integer limit, Integer offset, 
                                              String sortBy, String direction);

    MutationMeta getMetaMutationsBySampleListId(String molecularProfileId, String sampleListId, 
                                                List<Integer> entrezGeneIds, boolean snpOnly);

    
    List<Mutation> getMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds,
                                                                        List<Integer> entrezGeneIds, boolean snpOnly,
                                                                        String projection, Integer limit,
                                                                        Integer offset, String sortBy, String direction);

    List<Mutation> getMutationsInMultipleMolecularProfilesByGeneQueries(List<String> molecularProfileIds,
                                                                        List<String> sampleIds,
                                                                        boolean snpOnly,
                                                                        String projection,
                                                                        Integer limit,
                                                                        Integer offset,
                                                                        String sortBy,
                                                                        String direction,
                                                                        List<GeneFilterQuery> geneQueries);

    MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds,
                                                             List<Integer> entrezGeneIds, boolean snpOnly);

    MutationMeta getMetaMutationsBySampleIds(String molecularProfileId, List<String> sampleIds, 
                                             List<Integer> entrezGeneIds, boolean snpOnly);

    MutationCountByPosition getMutationCountByPosition(Integer entrezGeneId, Integer proteinPosStart, 
                                                       Integer proteinPosEnd);

    GenomicDataCountItem getMutationCountsByType(List<String> molecularProfileIds, List<String> sampleIds,
                                                List<Integer> entrezGeneIds, boolean snpOnly, String profileType);
}
