/**
 *
 * @author jiaojiao
 */
package org.cbioportal.persistence;


import java.util.List;
import java.util.Set;
import org.cbioportal.model.CNSegmentData;

public interface CNSegmentRepository {
    List<CNSegmentData> getCNSegmentData(String cancerStudyId, Set<String> chromosomes, List<String> sampleIds);
}