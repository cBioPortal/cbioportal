/**
 *
 * @author jiaojiao
 */
package org.cbioportal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.cbioportal.model.CNSegmentData;
import org.cbioportal.persistence.CNSegmentRepository;
import org.cbioportal.service.CNSegmentService;

@Service
public class CNSegmentServiceImpl implements CNSegmentService {

    @Autowired
    private CNSegmentRepository cnSegmentRepository;

    @Override
    public List<CNSegmentData> getCNSegmentData(String cancerStudyId, List<String> chromosomes, List<String> sampleIds) {
        return cnSegmentRepository.getCNSegmentData(cancerStudyId, chromosomes, sampleIds);
    }

}