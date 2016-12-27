package org.cbioportal.persistence.mybatis;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.SNPCount;

public interface MutationalSignatureMapper {
	List<SNPCount> getSNPCountsBySampleId(@Param("geneticProfileStableId") String geneticProfileStableId, @Param("sampleStableIds") List<String> sampleStableIds);
}
