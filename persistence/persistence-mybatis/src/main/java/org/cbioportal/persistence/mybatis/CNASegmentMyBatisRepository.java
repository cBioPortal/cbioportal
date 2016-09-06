/**
 *
 * @author jiaojiao
 */
package org.cbioportal.persistence.mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.cbioportal.model.CNASegmentData;
import org.cbioportal.persistence.CNASegmentRepository;

@Repository
public class CNASegmentMyBatisRepository implements CNASegmentRepository {

    @Autowired
    CNASegmentMapper cnaSegmentMapper;

    public List<CNASegmentData> getCNASegmentData(String cancerStudyId, List<String> chromosomes, List<String> sampleIds) {

        return cnaSegmentMapper.getCNASegmentData(cancerStudyId, chromosomes, sampleIds);
    }
}