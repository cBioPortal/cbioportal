package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SignificantCopyNumberRegionRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SignificantCopyNumberRegionMyBatisRepository
    implements SignificantCopyNumberRegionRepository {
    @Autowired
    private SignificantCopyNumberRegionMapper significantCopyNumberRegionMapper;

    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<Gistic> getSignificantCopyNumberRegions(
        String studyId,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    ) {
        return significantCopyNumberRegionMapper.getSignificantCopyNumberRegions(
            studyId,
            projection,
            pageSize,
            offsetCalculator.calculate(pageSize, pageNumber),
            sortBy,
            direction
        );
    }

    @Override
    public BaseMeta getMetaSignificantCopyNumberRegions(String studyId) {
        return significantCopyNumberRegionMapper.getMetaSignificantCopyNumberRegions(
            studyId
        );
    }

    @Override
    public List<GisticToGene> getGenesOfRegions(List<Long> gisticRoiIds) {
        return significantCopyNumberRegionMapper.getGenesOfRegions(
            gisticRoiIds
        );
    }
}
