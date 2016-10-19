package org.cbioportal.service;

import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.dto.AltCount;

import java.util.List;
import java.util.Map;
import org.cbioportal.persistence.dto.PositionMutationCount;

public interface MutationService {

    List<Mutation> getMutationsDetailed(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                        List<String> sampleStableIds, String sampleListStableId);

    List<AltCount> getMutationsCounts(String type, String hugoGeneSymbol, Integer start, Integer end,
                                      List<String> cancerStudyIdentifiers, Boolean perStudy);
    
    List<PositionMutationCount> getPositionMutationCounts(Map<String, List<Integer>> hugoGeneSymbolToPositions);
    
}
