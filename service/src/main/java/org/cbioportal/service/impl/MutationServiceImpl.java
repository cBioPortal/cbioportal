package org.cbioportal.service.impl;

import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.dto.SampleMutationCount;
import org.cbioportal.service.MutationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MutationServiceImpl implements MutationService {

    @Autowired
    private MutationRepository mutationRepository;

    public List<Mutation> getMutations(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                       List<String> sampleStableIds, String sampleListStableId) {

        return mutationRepository.getMutations(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds,
                sampleListStableId);
    }

    public List<SampleMutationCount> getMutationCounts(String geneticProfileStableId, List<String> sampleStableIds) {

        return mutationRepository.getMutationCounts(geneticProfileStableId, sampleStableIds);
    }
}
