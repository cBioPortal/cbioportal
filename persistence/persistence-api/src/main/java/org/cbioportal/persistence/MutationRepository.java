package org.cbioportal.persistence;

import org.cbioportal.model.Mutation;

import java.util.List;

public interface MutationRepository {

    List<Mutation> getMutations(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                List<String> sampleStableIds, String sampleListStableId);
}
