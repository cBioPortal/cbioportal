package org.mskcc.cbio.portal.repository;

import java.util.List;
import org.mskcc.cbio.portal.model.SNPCount;

public interface MutationalSignatureRepository {
    List<SNPCount> getSNPCounts(
        String geneticProfileStableId,
        List<String> sampleStableIds
    );
    List<SNPCount> getSNPCounts(String geneticProfileStableId);
}
