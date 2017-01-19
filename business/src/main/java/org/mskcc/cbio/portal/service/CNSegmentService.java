/**
 *
 * @author jiaojiao
 */
package org.mskcc.cbio.portal.service;

import org.mskcc.cbio.portal.model.CNSegmentData;

import java.util.List;

public interface CNSegmentService {

    List<CNSegmentData> getCNSegmentData(String cancerStudyId, List<String> chromosomes, List<String> sampleIds);
    String getCNSegmentFile(String cancerStudyId, List<String> sampleIds);
}