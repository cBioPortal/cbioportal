package org.cbioportal.service;

import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.dto.AltCount;
import org.mskcc.cbio.portal.dao.DaoException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MutationService {

    List<Mutation> getMutationsDetailed(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                        List<String> sampleStableIds, String sampleListStableId);

    Map<String,List> getMutationMatrix(List<String> sampleStableIds, String mutationGeneticProfileStableId,
                                       String mrnaGeneticProfileStableId, String cnaGeneticProfileStableId,
                                       String drugType) throws IOException, DaoException;

    Map<String, Integer> getMutationCount(String mutationGeneticProfileStableId, List<String> sampleStableIds);

    List<Map<String,Object>> getSmg(String mutationGeneticProfileStableId) throws DaoException;
}
