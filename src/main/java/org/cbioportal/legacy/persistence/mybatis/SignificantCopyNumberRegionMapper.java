package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.Gistic;
import org.cbioportal.legacy.model.GisticToGene;
import org.cbioportal.legacy.model.meta.BaseMeta;

import java.util.List;

public interface SignificantCopyNumberRegionMapper {
    
    List<Gistic> getSignificantCopyNumberRegions(String studyId, String projection, Integer limit, Integer offset,
                                                 String sortBy, String direction);
    
    BaseMeta getMetaSignificantCopyNumberRegions(String studyId);
    
    List<GisticToGene> getGenesOfRegions(List<Long> gisticRoiIds);
}
