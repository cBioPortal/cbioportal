package org.cbioportal.service;

import org.cbioportal.model.Mutation;

import java.util.List;

public interface MutationService {

    List<Mutation> getMutationsDetailed(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                        List<String> sampleStableIds, String sampleListStableId);
}
