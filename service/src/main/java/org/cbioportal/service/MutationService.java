package org.cbioportal.service;

import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.dto.AltCount;

import java.util.List;
import org.cbioportal.model.SNPCount;

public interface MutationService {

    List<Mutation> getMutationsDetailed(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                        List<String> sampleStableIds, String sampleListStableId);

    List<AltCount> getMutationsCounts(String type, String hugoGeneSymbol, Integer start, Integer end,
                                      List<String> cancerStudyIdentifiers, Boolean perStudy);
    
}
