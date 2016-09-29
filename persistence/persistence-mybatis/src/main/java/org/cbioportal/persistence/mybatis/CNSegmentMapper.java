/**
 *
 * @author jiaojiao
 */

package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;
import org.cbioportal.model.CNSegmentData;

public interface CNSegmentMapper {

    List<CNSegmentData> getCNSegmentData(@Param("cancerStudyId") String cancerStudyId, 
                                    @Param("chromosomes") List<String> chromosomes, 
                                    @Param("sampleIds") List<String> sampleIds);
}