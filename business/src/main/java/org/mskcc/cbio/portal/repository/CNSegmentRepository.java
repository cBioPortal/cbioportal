/**
 *
 * @author jiaojiao
 */
package org.mskcc.cbio.portal.repository;


import org.mskcc.cbio.portal.model.CNSegmentData;

import java.util.List;

public interface CNSegmentRepository {
    List<CNSegmentData> getCNSegmentData(String cancerStudyId, List<String> chromosomes, List<String> sampleIds);
}