package org.mskcc.cbio.portal.or_analysis;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.inference.TestUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
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
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNodeFactory factory = JsonNodeFactory.instance;
    private final ArrayNode result = new ArrayNode(factory);
    static DecimalFormat df=new DecimalFormat("0.000");
    
    public ORAnalysisDiscretizedDataProxy(
            int cancerStudyId, 
            int profileId, 
            String profileType, 
            List<Integer> alteredSampleIds, 
            List<Integer> unalteredSampleIds) throws DaoException, IllegalArgumentException, MathException {
        
        this.alteredSampleIds = alteredSampleIds;
        this.unalteredSampleIds = unalteredSampleIds;
        this.map = OverRepresentationAnalysisUtil.getValueMap(cancerStudyId, profileId, profileType, alteredSampleIds, unalteredSampleIds);
        
        if (!map.keySet().isEmpty()) {
            DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

            List<Long> genes = new ArrayList<Long>(map.keySet());
            for (int i = 0; i < map.size(); i++) {
                long _gene = genes.get(i);
                HashMap<Integer, String> singleGeneCaseValueMap = map.get(_gene);
                String _geneName = daoGeneOptimized.getGene(_gene).getHugoGeneSymbolAllCaps();
                
                ObjectNode _datum = mapper.createObjectNode();
                if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString())) {
                    _datum.put("Gene", _geneName);
                    _datum.put("%Altered", df.format(calcPct(singleGeneCaseValueMap, profileType, "altered")));
                    _datum.put("%Unaltered", df.format(calcPct(singleGeneCaseValueMap, profileType, "unaltered")));
                    _datum.put("Radio", df.format(calcPct(singleGeneCaseValueMap, profileType, "altered") / calcPct(singleGeneCaseValueMap, profileType, "unaltered")));
                    _datum.put("Direction/Tendency", "place holder");
                    _datum.put("p-Value", df.format(calcPval(singleGeneCaseValueMap, profileType)));
                    _datum.put("q-Value", "place holder");
                } else if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
                    _datum.put("Gene", _geneName);
                    _datum.put("%Altered", df.format(calcPct(singleGeneCaseValueMap, profileType, "altered")));
                    _datum.put("%Unaltered", df.format(calcPct(singleGeneCaseValueMap, profileType, "unaltered")));
                    _datum.put("Radio", df.format(calcPct(singleGeneCaseValueMap, profileType, "altered") / calcPct(singleGeneCaseValueMap, profileType, "unaltered")));
                    _datum.put("Direction/Tendency", "place holder");
                    _datum.put("p-Value", df.format(calcPval(singleGeneCaseValueMap, profileType)));
                    _datum.put("q-Value", "place holder");
                } else if (profileType.equals(GeneticAlterationType.MRNA_EXPRESSION.toString())) {
                    _datum.put("Gene", _geneName);
                    _datum.put("M-altered", df.format(calcMean(singleGeneCaseValueMap, "altered")));
                    _datum.put("M-unaltered", df.format(calcMean(singleGeneCaseValueMap, "unaltered")));
                    _datum.put("StD-Dev Altered", df.format(calcSTDev(singleGeneCaseValueMap, "altered")));
                    _datum.put("StD-Dev Unaltered", df.format(calcSTDev(singleGeneCaseValueMap, "unaltered")));
                    _datum.put("T-score", "place holder");
                    _datum.put("p-Value", df.format(calcPval(singleGeneCaseValueMap, profileType)));
                    _datum.put("q-Value", "place holder");
                }
                result.add(_datum);
            }
        } 
    }
    
    public ArrayNode getResult() {
        return result;
    }
    
    private double calcMean(HashMap<Integer, String> singleGeneCaseValueMap, String groupType) { // group type: altered or unaltered
        switch (groupType) {
            case "altered":
                int _index_altered = 0;
                double[] alteredArray = new double[alteredSampleIds.size()];
                for (Integer alteredSampleId: alteredSampleIds) {
                    if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                        alteredArray[_index_altered] = Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId));
                        _index_altered += 1;
                    }
                }
                return StatUtils.mean(alteredArray);
            case "unaltered":
                int _index_unaltered = 0;
                double[] unalteredArray = new double[unalteredSampleIds.size()];
                for (Integer unalteredSampleId: unalteredSampleIds) {
                    if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                        unalteredArray[_index_unaltered] = Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId));
                        _index_unaltered += 1;
                    }
                }
                return StatUtils.mean(unalteredArray);
            default:
                return Double.NaN; //error
        }
    }
    
    private double calcSTDev(HashMap<Integer, String> singleGeneCaseValueMap, String groupType) {
        switch (groupType) {
            case "altered":
                DescriptiveStatistics stats_altered = new DescriptiveStatistics();
                for (Integer alteredSampleId: alteredSampleIds) {
                    if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                        stats_altered.addValue(Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId)));
                    }
                }
                return stats_altered.getStandardDeviation();
            case "unaltered":
                DescriptiveStatistics stats_unaltered = new DescriptiveStatistics();
                double[] unalteredArray = new double[unalteredSampleIds.size()];
                for (Integer unalteredSampleId: unalteredSampleIds) {
                    if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                        stats_unaltered.addValue(Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId)));
                    }
                }
                return stats_unaltered.getStandardDeviation();
            default:
                return Double.NaN; //error
        }        
    
    }
    
    private double calcPct(HashMap<Integer, String> singleGeneCaseValueMap, String profileType, String groupType) { // group type: altered or unaltered
        
        double _result_pct = 0, _count = 0; //altered samples count
        
        if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString())) {
             switch (groupType) {
                case "altered":
                    for (Integer alteredSampleId: alteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                            if (Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId)) == 2.0 || 
                                Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId)) == -2.0) { 
                                _count += 1;
                            } 
                        }
                    }
                    _result_pct = (double)(_count/alteredSampleIds.size());
                    break;
                case "unaltered":
                    for (Integer unalteredSampleId: unalteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                            if (Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId)) == 2.0 || 
                                Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId)) == -2.0) { 
                                _count += 1;
                            }   
                        }  
                    } 
                    _result_pct = (double)(_count/unalteredSampleIds.size());
                    break;
            }
        } else if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
            switch (groupType) {
                case "altered":
                    for (Integer alteredSampleId: alteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                            if (!singleGeneCaseValueMap.get(alteredSampleId).equals("Non")) { 
                                _count += 1;
                            } 
                        }
                    }
                    _result_pct = (double)(_count/alteredSampleIds.size());
                    break;
                case "unaltered":
                    for (Integer unalteredSampleId: unalteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                            if (!singleGeneCaseValueMap.get(unalteredSampleId).equals("Non")) { 
                                _count += 1;
                            }   
                        }  
                    } 
                    _result_pct = (double)(_count/unalteredSampleIds.size());
                    break;
            }
        } else if (profileType.equals(GeneticAlterationType.MRNA_EXPRESSION.toString())) { //calculate mean
        }

        return _result_pct;
    
    }
    
    private double calcPval(HashMap<Integer, String> singleGeneCaseValueMap, String profileType) 
            throws IllegalArgumentException, MathException {
        double _p_value = 0.0;
        if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString()) || 
            profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString())) {
            _p_value = runFisherExactTest(singleGeneCaseValueMap, profileType);
        } else if (profileType.equals(GeneticAlterationType.MRNA_EXPRESSION.toString())) {
            _p_value = runTTest(singleGeneCaseValueMap, profileType);
        }
        return _p_value;
    }
    
    private double runTTest(HashMap<Integer, String> singleGeneCaseValueMap, String profileType) 
            throws IllegalArgumentException, MathException {
        
        double[] unalteredArray = new double[unalteredSampleIds.size()];
        double[] alteredArray = new double[alteredSampleIds.size()];
        int _index_unaltered = 0, _index_altered = 0;
        for (Integer alteredSampleId: alteredSampleIds) {
            if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                alteredArray[_index_altered] = Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId));
                _index_altered += 1;
            }
        }
        
        for (Integer unalteredSampleId: unalteredSampleIds) {
            if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                unalteredArray[_index_unaltered] = Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId));
                _index_unaltered += 1;
            }
        }
        
        if (alteredArray.length<2 || unalteredArray.length<2) return Double.NaN;
        else {
            double pvalue = TestUtils.tTest(alteredArray, unalteredArray);
            return pvalue;
        }
    }
    
    private double runFisherExactTest(HashMap<Integer, String> singleGeneCaseValueMap, String profileType) {
        
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
