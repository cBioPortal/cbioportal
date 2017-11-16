package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.VariantCount;
import org.cbioportal.persistence.VariantCountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VariantCountMyBatisRepository implements VariantCountRepository {
    
    @Autowired
    private VariantCountMapper variantCountMapper;
    
    @Override
    public List<VariantCount> fetchVariantCounts(String molecularProfileId, List<Integer> entrezGeneIds, 
                                                 List<String> keywords) {
        
        return variantCountMapper.fetchVariantCounts(molecularProfileId, entrezGeneIds, keywords);
    }
}
