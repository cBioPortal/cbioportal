package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.Mutation;

import java.util.List;

public interface MutationMapper {

    List<Mutation> getMutations(@Param("geneticProfileStableIds") List<String> geneticProfileStableIds,
                                @Param("hugoGeneSymbols") List<String> hugoGeneSymbols,
                                @Param("sampleStableIds") List<String> sampleStableIds,
                                @Param("sampleListStableId") String sampleListStableId);
}
