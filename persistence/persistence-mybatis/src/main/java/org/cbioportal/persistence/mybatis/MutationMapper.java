package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.model.MutationSampleCountByKeyword;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.model.meta.MutationMeta;

import java.util.List;

public interface MutationMapper {

    List<Mutation> getMutations(String geneticProfileId, List<String> sampleIds, String projection, Integer limit, 
                                Integer offset, String sortBy, String direction);

    MutationMeta getMetaMutations(String geneticProfileId, List<String> sampleIds);
    
    List<MutationSampleCountByGene> getSampleCountByEntrezGeneIds(String geneticProfileId, List<Integer> entrezGeneIds);
    
    List<MutationSampleCountByKeyword> getSampleCountByKeywords(String geneticProfileId, List<String> keywords);
}
