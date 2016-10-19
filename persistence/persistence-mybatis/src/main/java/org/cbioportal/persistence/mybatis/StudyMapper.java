package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface StudyMapper {

    List<CancerStudy> getAllStudies(@Param("projection") String projection,
                                    @Param("limit") Integer limit,
                                    @Param("offset") Integer offset,
                                    @Param("sortBy") String sortBy,
                                    @Param("direction") String direction);

    BaseMeta getMetaStudies();

    CancerStudy getStudy(@Param("studyId") String studyId,
                         @Param("projection") String projection);
}
