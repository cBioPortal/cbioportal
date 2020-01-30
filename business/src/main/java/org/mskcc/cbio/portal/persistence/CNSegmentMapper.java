/**
 *
 * @author jiaojiao
 */

package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.mskcc.cbio.portal.model.CNSegmentData;

public interface CNSegmentMapper {
    List<CNSegmentData> getCNSegmentData(
        String cancerStudyId,
        List<String> chromosomes,
        List<String> sampleIds
    );
}
