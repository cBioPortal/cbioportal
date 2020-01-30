package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.SNPCount;

public interface MutationalSignatureMapper {
    List<SNPCount> getSNPCountsBySampleId(
        @Param("geneticProfileStableId") String geneticProfileStableId,
        @Param("sampleStableIds") List<String> sampleStableIds
    );
}
