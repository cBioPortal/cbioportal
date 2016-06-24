package org.cbioportal.service.impl;

import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.service.MutationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MutationServiceImpl implements MutationService {

    @Autowired
    private MutationRepository mutationRepository;

    public List<Mutation> getMutationsDetailed(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                               List<String> sampleStableIds, String sampleListStableId) {

        return mutationRepository.getMutationsDetailed(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds,
                sampleListStableId);
    }
}
