package org.cbioportal.service.impl;

import java.util.LinkedList;
import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.dto.AltCount;
import org.cbioportal.service.MutationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.cbioportal.persistence.dto.PositionMutationCount;

@Service
public class MutationServiceImpl implements MutationService {

    @Autowired
    private MutationRepository mutationRepository;

    public List<Mutation> getMutationsDetailed(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                               List<String> sampleStableIds, String sampleListStableId) {

        return mutationRepository.getMutationsDetailed(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds,
                sampleListStableId);
    }

    public List<AltCount> getMutationsCounts(String type, String hugoGeneSymbol, Integer start, Integer end,
                                             List<String> cancerStudyIdentifiers, Boolean perStudy) {

        return mutationRepository.getMutationsCounts(type, hugoGeneSymbol, start, end, cancerStudyIdentifiers,
                perStudy);
    }
    
    public List<PositionMutationCount> getPositionMutationCounts(Map<String, List<Integer>> hugoGeneSymbolToPositions) {
	    List<PositionMutationCount> ret = new LinkedList<PositionMutationCount>();
	    for (Entry<String,List<Integer>> entry: hugoGeneSymbolToPositions.entrySet()) {
		    ret.addAll(mutationRepository.getPositionMutationCounts(entry.getKey(), entry.getValue()));
	    }
	    return ret;
    }
}
