/**
 *
 * @author jiaojiao
 */

package org.mskcc.cbio.portal.persistence;

import org.mskcc.cbio.portal.model.CNSegmentData;

import java.util.List;

public interface CNSegmentMapper {

    List<CNSegmentData> getCNSegmentData(String cancerStudyId, List<String> chromosomes, List<String> sampleIds);
}