package org.mskcc.cbio.portal.or_analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.stats.FisherExact;

/**
 * pre-calculate/re-format input raw data based on different profiles
 * then return result (p-value) of fisher exact test
 *
 * @date Mar 16, 2015
 * @author suny1
 */
public class ORAnalysisDiscretizedDataProxy {
    
    private final Map<Long, HashMap<Integer, String>> map;
    private final List<Integer> alteredSampleIds;
    private final List<Integer> unalteredSampleIds;
    private final StringBuilder result = new StringBuilder();
    
    public ORAnalysisDiscretizedDataProxy(
            int cancerStudyId, 
            int profileId, 
            String profileType, 
            List<Integer> alteredSampleIds, 
            List<Integer> unalteredSampleIds) throws DaoException {
        
        this.alteredSampleIds = alteredSampleIds;
        this.unalteredSampleIds = unalteredSampleIds;
        this.map = OverRepresentationAnalysisUtil.getValueMap(cancerStudyId, profileId, profileType, alteredSampleIds, unalteredSampleIds);
        
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
            
        List<Long> genes = new ArrayList<Long>(map.keySet());
        for (int i = 0; i < map.size(); i++) {
            long _gene = genes.get(i);
            HashMap<Integer, String> singleGeneCaseValueMap = map.get(_gene);
            String _geneName = daoGeneOptimized.getGene(_gene).getHugoGeneSymbolAllCaps();
            double pValue = calcFishExactTest(singleGeneCaseValueMap, profileType);
            result.append(_geneName);
            result.append(":");
            result.append(pValue);
            result.append("|");
        }
         
    }
    
    public String getResult() {
        return result.toString();
    }
    
    private double calcFishExactTest(HashMap<Integer, String> singleGeneCaseValueMap, String profileType) {
        
        int a = 0, //non altered
            b = 0, //x non altered, y altered
            c = 0, //x altered, y non altered
            d = 0; //both alered
        
        for (Integer alteredSampleId: alteredSampleIds) {
            if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                
                if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString())) {
                    Double value = Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId));
                    if (value == 2.0 || value == -2.0) {
                        d += 1;
                    } else {
                        c += 1;
                    }
                } else if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
                    String value = singleGeneCaseValueMap.get(alteredSampleId);
                    if (value.equals("Non")) {
                        c += 1;
                    } else {
                        d += 1;
                    }
                }

            } 
        }
        
        for (Integer unalteredSampleId: unalteredSampleIds) {
            if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString())) { 
                    Double value = Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId));
                    if (value == 2.0 || value == -2.0) {
                        b += 1;
                    } else {
                        a += 1;
                    }
                } else if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
                    String value = singleGeneCaseValueMap.get(unalteredSampleId);
                    if (value.equals("Non")) {
                        a += 1;
                    } else {
                        b += 1;
                    }
                } 
            } 
        }
        
        
        FisherExact fisher = new FisherExact(a + b + c + d);
        return fisher.getCumlativeP(a, b, c, d);
        
    }
    
}
