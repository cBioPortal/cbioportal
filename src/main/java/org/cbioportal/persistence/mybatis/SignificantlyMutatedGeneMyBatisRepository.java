package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SignificantlyMutatedGeneRepository;
import org.cbioportal.persistence.mybatis.util.PaginationCalculator;
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
