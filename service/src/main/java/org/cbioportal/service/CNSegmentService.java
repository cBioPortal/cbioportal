/**
 *
 * @author jiaojiao
 */
package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.CNSegmentData;

public interface CNSegmentService {

    List<CNSegmentData> getCNSegmentData(String cancerStudyId, List<String> chromosomes, List<String> sampleIds);
}