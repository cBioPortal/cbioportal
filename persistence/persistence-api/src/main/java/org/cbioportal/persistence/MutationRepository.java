package org.cbioportal.persistence;

import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.dto.SampleMutationCount;

import java.util.List;

public interface MutationRepository {

    List<Mutation> getMutations(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                List<String> sampleStableIds, String sampleListStableId);

    List<SampleMutationCount> getMutationCounts(String geneticProfileStableId,
                                                List<String> sampleStableIds);
}
