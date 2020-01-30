/**
 *
 * @author jiaojiao
 */
package org.mskcc.cbio.portal.service;

import java.util.List;
import org.mskcc.cbio.portal.model.CNSegmentData;

public interface CNSegmentService {
    List<CNSegmentData> getCNSegmentData(
        String cancerStudyId,
        List<String> chromosomes,
        List<String> sampleIds
    );
    String getCNSegmentFile(String cancerStudyId, List<String> sampleIds);
}
