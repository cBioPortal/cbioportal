package org.cbioportal.persistence.mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.cbioportal.model.COSMICCount;
import org.cbioportal.persistence.COSMICCountRepository;

@Repository
public class COSMICCountMyBatisRepository implements COSMICCountRepository {

    @Autowired
    COSMICCountMapper cosmicCountMapper;

    public List<COSMICCount> getCOSMICCountsByKeywords(List<String> keywords) {
	    return cosmicCountMapper.getCOSMICCountsByKeywords(keywords);
    }
}
