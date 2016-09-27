/**
 *
 * @author jiaojiao
 */
package org.cbioportal.persistence.mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.cbioportal.model.CNSegmentData;
import org.cbioportal.persistence.CNSegmentRepository;

@Repository
public class CNSegmentMyBatisRepository implements CNSegmentRepository {

    @Autowired
    CNSegmentMapper cnSegmentMapper;
    
    @Override
    public List<CNSegmentData> getCNSegmentData(String cancerStudyId, List<String> chromosomes, List<String> sampleIds) {

        return cnSegmentMapper.getCNSegmentData(cancerStudyId, chromosomes, sampleIds);
    }
}