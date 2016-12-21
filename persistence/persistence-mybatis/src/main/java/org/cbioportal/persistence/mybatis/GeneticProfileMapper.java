package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface GeneticProfileMapper {

    List<GeneticProfile> getAllGeneticProfiles(@Param("studyId") String studyId,
                                               @Param("projection") String projection,
                                               @Param("limit") Integer limit,
                                               @Param("offset") Integer offset,
                                               @Param("sortBy") String sortBy,
                                               @Param("direction") String direction);

    BaseMeta getMetaGeneticProfiles(@Param("studyId") String studyId);

    GeneticProfile getGeneticProfile(@Param("geneticProfileId") String geneticProfileId,
                                     @Param("projection") String projection);
}
