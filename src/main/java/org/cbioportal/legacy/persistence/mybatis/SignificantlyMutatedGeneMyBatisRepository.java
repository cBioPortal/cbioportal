package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.MutSig;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.SignificantlyMutatedGeneRepository;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SignificantlyMutatedGeneMyBatisRepository implements SignificantlyMutatedGeneRepository {

    @Autowired
    private SignificantlyMutatedGeneMapper significantlyMutatedGeneMapper;

    @Override
    public List<MutSig> getSignificantlyMutatedGenes(String studyId, String projection, Integer pageSize, 
                                                     Integer pageNumber, String sortBy, String direction) {

        return significantlyMutatedGeneMapper.getSignificantlyMutatedGenes(studyId, projection, pageSize,
            PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSignificantlyMutatedGenes(String studyId) {

        return significantlyMutatedGeneMapper.getMetaSignificantlyMutatedGenes(studyId);
    }
}
