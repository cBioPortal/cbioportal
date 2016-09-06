/**
 *
 * @author jiaojiao
 */

package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import org.cbioportal.model.CNASegmentData;

public interface CNASegmentMapper {

    List<CNASegmentData> getCNASegmentData(@Param("cancerStudyId") String cancerStudyId, 
                                    @Param("chromosomes") List<String> chromosomes, 
                                    @Param("sampleIds") List<String> sampleIds);
}