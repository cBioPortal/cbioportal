package org.cbioportal.service;

import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.dto.SampleMutationCount;

import java.util.List;
import java.util.Map;

public interface MutationService {

    List<Mutation> getMutations(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                List<String> sampleStableIds, String sampleListStableId);

    List<SampleMutationCount> getMutationCounts(String geneticProfileStableId, List<String> sampleStableIds);
}
