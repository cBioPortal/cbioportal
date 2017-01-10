package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface MutationMapper {

    List<Mutation> getMutations(String geneticProfileId, List<String> sampleIds, String projection, Integer limit, 
                                Integer offset, String sortBy, String direction);

    BaseMeta getMetaMutations(String geneticProfileId, List<String> sampleIds);
}
