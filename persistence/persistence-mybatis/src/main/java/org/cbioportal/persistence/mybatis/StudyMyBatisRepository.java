package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudyMyBatisRepository implements StudyRepository {

    @Autowired
    private StudyMapper studyMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<CancerStudy> getAllStudies(String projection, Integer pageSize, Integer pageNumber,
                                           String sortBy, String direction) {

        return studyMapper.getAllStudies(projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy,
                direction);
    }

    @Override
    public BaseMeta getMetaStudies() {
        return studyMapper.getMetaStudies();
    }

    @Override
    public CancerStudy getStudy(String studyId, String projection) {
        return studyMapper.getStudy(studyId, projection);
    }
}
