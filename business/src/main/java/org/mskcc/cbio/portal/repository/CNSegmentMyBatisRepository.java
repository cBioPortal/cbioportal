/**
 *
 * @author jiaojiao
 */
package org.mskcc.cbio.portal.repository;

import java.util.List;
import org.mskcc.cbio.portal.model.CNSegmentData;
import org.mskcc.cbio.portal.persistence.CNSegmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CNSegmentMyBatisRepository implements CNSegmentRepository {
    @Autowired
    CNSegmentMapper cnSegmentMapper;

    @Override
    public List<CNSegmentData> getCNSegmentData(
        String cancerStudyId,
        List<String> chromosomes,
        List<String> sampleIds
    ) {
        return cnSegmentMapper.getCNSegmentData(
            cancerStudyId,
            chromosomes,
            sampleIds
        );
    }

    public void setCnSegmentMapper(CNSegmentMapper cnSegmentMapper) {
        this.cnSegmentMapper = cnSegmentMapper;
    }
}
