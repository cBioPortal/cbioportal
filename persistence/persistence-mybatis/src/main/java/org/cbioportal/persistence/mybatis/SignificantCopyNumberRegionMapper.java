package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
import org.cbioportal.model.meta.BaseMeta;

public interface SignificantCopyNumberRegionMapper {
    List<Gistic> getSignificantCopyNumberRegions(
        String studyId,
        String projection,
        Integer limit,
        Integer offset,
        String sortBy,
        String direction
    );

    BaseMeta getMetaSignificantCopyNumberRegions(String studyId);

    List<GisticToGene> getGenesOfRegions(List<Long> gisticRoiIds);
}
