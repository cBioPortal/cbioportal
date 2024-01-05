package org.cbioportal.persistence.mysql;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.meta.MutationMeta;
import org.springframework.context.annotation.Profile;

import java.util.List;

public interface MutationMapper {

    List<Mutation> getMutationsBySampleListId(String molecularProfileId, String sampleListId, List<Integer> entrezGeneIds,
                                              Boolean snpOnly, String projection, Integer limit, Integer offset, 
                                              String sortBy, String direction);

    MutationMeta getMetaMutationsBySampleListId(String molecularProfileId, String sampleListId, 
                                                List<Integer> entrezGeneIds, Boolean snpOnly);

    
    List<Mutation> getMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds,
                                                                        List<Integer> entrezGeneIds, Boolean snpOnly,
                                                                        String projection, Integer limit,
                                                                        Integer offset, String sortBy, String direction);

    List<Mutation> getMutationsInMultipleMolecularProfilesByGeneQueries(List<String> molecularProfileIds,
                                                                        List<String> sampleIds,
                                                                        Boolean snpOnly,
                                                                        String projection,
                                                                        Integer limit,
                                                                        Integer offset,
                                                                        String sortBy,
                                                                        String direction,
                                                                        List<GeneFilterQuery> geneQueries);

    MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, List<String> sampleIds,
                                                             List<Integer> entrezGeneIds, Boolean snpOnly);

    MutationMeta getMetaMutationsBySampleIds(String molecularProfileId, List<String> sampleIds, 
                                             List<Integer> entrezGeneIds, Boolean snpOnly);

    MutationCountByPosition getMutationCountByPosition(Integer entrezGeneId, Integer proteinPosStart, 
                                                       Integer proteinPosEnd);

}
