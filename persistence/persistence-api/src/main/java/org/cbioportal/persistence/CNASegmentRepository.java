/**
 *
 * @author jiaojiao
 */
package org.cbioportal.persistence;


import java.util.List;
import org.cbioportal.model.CNASegmentData;

public interface CNASegmentRepository {
    List<CNASegmentData> getCNASegmentData(String cancerStudyId, List<String> chromosomes, List<String> sampleIds);
}