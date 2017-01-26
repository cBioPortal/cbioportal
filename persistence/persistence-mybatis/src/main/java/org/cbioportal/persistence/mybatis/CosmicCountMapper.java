package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import org.cbioportal.model.CosmicCount;

public interface CosmicCountMapper {

	List<CosmicCount> getCOSMICCountsByKeywords(@Param("keywords") List<String> keywords);
}
