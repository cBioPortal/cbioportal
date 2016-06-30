package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.SV;
import org.cbioportal.persistence.SVRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 *
 * @author jake
 */

@Repository
public class SVMyBatisRepository implements SVRepository{
    
    @Autowired
    SVMapper svMapper;
    
    @Override
    public List<SV> getSVs(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols, List<String> sampleStableIds){
        return svMapper.getSVs(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
    }
}
