package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface StudyMapper {

    List<CancerStudy> getAllStudies(String projection, Integer limit, Integer offset, String sortBy, String direction);

    BaseMeta getMetaStudies();

    CancerStudy getStudy(String studyId, String projection);
}
