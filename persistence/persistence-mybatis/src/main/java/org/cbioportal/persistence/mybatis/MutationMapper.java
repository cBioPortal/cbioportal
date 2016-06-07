package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.dto.DBAltCount;

import java.util.List;

public interface MutationMapper {

    List<Mutation> getMutations(@Param("geneticProfileStableIds") List<String> geneticProfileStableIds,
                                @Param("hugoGeneSymbols") List<String> hugoGeneSymbols,
                                @Param("sampleStableIds") List<String> sampleStableIds,
                                @Param("sampleListStableId") String sampleListStableId);

    List<DBAltCount> getMutationsCounts(@Param("type") String type, @Param("gene") String gene,
                                        @Param("start") Integer start, @Param("end") Integer end,
                                        @Param("studyIds") List<String> studyIds, @Param("perStudy") Boolean perStudy);
}
