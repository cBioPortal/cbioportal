package org.cbioportal.service.impl;

import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SignificantCopyNumberRegionRepository;
import org.cbioportal.service.SignificantCopyNumberRegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SignificantCopyNumberRegionServiceImpl implements SignificantCopyNumberRegionService {
    
    @Autowired
    private SignificantCopyNumberRegionRepository significantCopyNumberRegionRepository;
    
    @Override
    public List<Gistic> getSignificantCopyNumberRegions(String studyId, String projection, Integer pageSize, 
                                                        Integer pageNumber, String sortBy, String direction) {
        
        List<Gistic> gisticList = significantCopyNumberRegionRepository.getSignificantCopyNumberRegions(studyId, 
            projection, pageSize, pageNumber, sortBy, direction);
        
        if (!projection.equals("ID")) {
            
            List<GisticToGene> gisticToGeneList = significantCopyNumberRegionRepository.getGenesOfRegions(gisticList
                .stream().map(Gistic::getGisticRoiId).collect(Collectors.toList()));

            gisticList.forEach(g -> g.setGenes(gisticToGeneList.stream().filter(p -> p.getGisticRoiId()
                .equals(g.getGisticRoiId())).collect(Collectors.toList())));
        }
        
        return gisticList;
    }

    @Override
    public BaseMeta getMetaSignificantCopyNumberRegions(String studyId) {
        
        return significantCopyNumberRegionRepository.getMetaSignificantCopyNumberRegions(studyId);
    }
}
