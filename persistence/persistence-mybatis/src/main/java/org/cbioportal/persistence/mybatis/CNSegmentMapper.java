/**
 *
 * @author jiaojiao
 */

package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.cbioportal.model.CNSegmentData;

public interface CNSegmentMapper {

    List<CNSegmentData> getCNSegmentData(String cancerStudyId, List<String> chromosomes, List<String> sampleIds);
}