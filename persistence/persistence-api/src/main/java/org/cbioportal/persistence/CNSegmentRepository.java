/**
 *
 * @author jiaojiao
 */
package org.cbioportal.persistence;


import java.util.List;
import org.cbioportal.model.CNSegmentData;

public interface CNSegmentRepository {
    List<CNSegmentData> getCNSegmentData(String cancerStudyId, List<String> chromosomes, List<String> sampleIds);
}