package org.cbioportal.service.impl;

import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.dto.AltCount;
import org.cbioportal.service.MutationService;
import org.mskcc.cbio.portal.dao.DaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MutationServiceImpl implements MutationService {

    @Autowired
    private MutationRepository mutationRepository;
    @Autowired
    private MutationMatrixCalculator mutationMatrixCalculator;
    @Autowired
    private MutationCountCalculator mutationCountCalculator;
    @Autowired
    private SmgCalculator smgCalculator;

    public List<Mutation> getMutationsDetailed(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                               List<String> sampleStableIds, String sampleListStableId) {

        return mutationRepository.getMutationsDetailed(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds,
                sampleListStableId);
    }

    @Override
    public Map<String,List> getMutationMatrix(List<String> sampleStableIds, String mutationGeneticProfileStableId,
                                              String mrnaGeneticProfileStableId, String cnaGeneticProfileStableId,
                                              String drugType) throws IOException, DaoException {

        return mutationMatrixCalculator.calculate(sampleStableIds, mutationGeneticProfileStableId,
                mrnaGeneticProfileStableId, cnaGeneticProfileStableId, drugType);
    }

    @Override
    public Map<String, Integer> getMutationCount(String mutationGeneticProfileStableId, List<String> sampleStableIds) {

        return mutationCountCalculator.calculate(mutationGeneticProfileStableId, sampleStableIds);
    }

    @Override
    public List<Map<String, Object>> getSmg(String mutationGeneticProfileStableId, String sampleStableIds)
            throws DaoException {

        return smgCalculator.calculate(mutationGeneticProfileStableId, sampleStableIds);
    }
}
