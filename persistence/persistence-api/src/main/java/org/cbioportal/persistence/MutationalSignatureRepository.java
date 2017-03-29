package org.cbioportal.persistence;

import java.util.List;
import org.cbioportal.model.SNPCount;

public interface MutationalSignatureRepository {
	List<SNPCount> getSNPCounts(String geneticProfileStableId, List<String> sampleStableIds);
	List<SNPCount> getSNPCounts(String geneticProfileStableId);
}
