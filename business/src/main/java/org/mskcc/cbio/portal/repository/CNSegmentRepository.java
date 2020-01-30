/**
 *
 * @author jiaojiao
 */
package org.mskcc.cbio.portal.repository;

import java.util.List;
import org.mskcc.cbio.portal.model.CNSegmentData;

public interface CNSegmentRepository {
    List<CNSegmentData> getCNSegmentData(
        String cancerStudyId,
        List<String> chromosomes,
        List<String> sampleIds
    );
}
