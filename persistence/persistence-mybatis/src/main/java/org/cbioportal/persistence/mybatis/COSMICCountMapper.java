package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import org.cbioportal.model.COSMICCount;

public interface COSMICCountMapper {

	List<COSMICCount> getCOSMICCountsByKeywords(@Param("keywords") List<String> keywords);
}
