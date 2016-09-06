/**
 *
 * @author jiaojiao
 */
package org.cbioportal.service;

import java.util.List;
import org.cbioportal.model.CNASegmentData;

public interface CNASegmentService {

    List<CNASegmentData> getCNASegmentData(String cancerStudyId, List<String> hugoGeneSymbols, List<String> sampleIds);
}