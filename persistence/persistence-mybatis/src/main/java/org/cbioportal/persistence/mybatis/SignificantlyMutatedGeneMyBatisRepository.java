package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SignificantlyMutatedGeneRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SignificantlyMutatedGeneMyBatisRepository
    implements SignificantlyMutatedGeneRepository {
    @Autowired
    private SignificantlyMutatedGeneMapper significantlyMutatedGeneMapper;

    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<MutSig> getSignificantlyMutatedGenes(
        String studyId,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    ) {
        return significantlyMutatedGeneMapper.getSignificantlyMutatedGenes(
            studyId,
            projection,
            pageSize,
            offsetCalculator.calculate(pageSize, pageNumber),
            sortBy,
            direction
        );
    }

    @Override
    public BaseMeta getMetaSignificantlyMutatedGenes(String studyId) {
        return significantlyMutatedGeneMapper.getMetaSignificantlyMutatedGenes(
            studyId
        );
    }
}
