package org.cbioportal.persistence;

import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface SignificantCopyNumberRegionRepository {
    
    List<Gistic> getSignificantCopyNumberRegions(String studyId, String projection, Integer pageSize, 
                                                 Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaSignificantCopyNumberRegions(String studyId);

    List<GisticToGene> getGenesOfRegions(List<Long> gisticRoiIds);
}
