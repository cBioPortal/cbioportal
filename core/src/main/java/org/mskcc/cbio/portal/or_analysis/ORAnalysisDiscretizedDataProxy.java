package org.mskcc.cbio.portal.or_analysis;

import java.util.*;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.inference.TestUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.stats.BenjaminiHochbergFDR;
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

    private final String COL_NAME_GENE = "Gene";
    private final String COL_NAME_CYTOBAND = "Cytoband";
    private final String COL_NAME_PCT_ALTERED = "percentage of alteration in altered group";
    private final String COL_NAME_PCT_UNALTERED = "percentage of alteration in unaltered group";
    private final String COL_NAME_RATIO = "Log Ratio";
    private final String COL_NAME_DIRECTION = "Direction/Tendency";
    private final String COL_NAME_P_VALUE = "p-Value";
    private final String COL_NAME_Q_VALUE = "q-Value";
    private final String COL_NAME_MEAN_ALTERED = "mean of alteration in altered group";
    private final String COL_NAME_MEAN_UNALTERED = "mean of alteration in unaltered group";
    private final String COL_NAME_STDEV_ALTERED = "standard deviation of alteration in altered group";
    private final String COL_NAME_STDEV_UNALTERED = "standard deviation of alteration in unaltered group";
    
    private String copyNumType = "none";
    
    public ORAnalysisDiscretizedDataProxy(
            int cancerStudyId, 
            int profileId,
            String profileType, 
            List<Integer> alteredSampleIds, 
            List<Integer> unalteredSampleIds,
            String copyNumType,
            String proteinExpType,
            String[] queryGenes,
            String geneSet) throws DaoException, IllegalArgumentException, MathException {
        
        this.alteredSampleIds = alteredSampleIds;
        this.unalteredSampleIds = unalteredSampleIds;
        this.map = OverRepresentationAnalysisUtil.getValueMap(cancerStudyId, profileId, profileType, alteredSampleIds, unalteredSampleIds, geneSet, proteinExpType);
        this.copyNumType = copyNumType;

        if (!map.keySet().isEmpty()) {
            DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

            ArrayList<ObjectNode> _result = new ArrayList<ObjectNode>();
            List<Long> genes = new ArrayList<Long>(map.keySet());
            for (int i = 0; i < map.size(); i++) {
                long _gene = genes.get(i);
                HashMap<Integer, String> singleGeneCaseValueMap = map.get(_gene);

                //clean up empty values case-value map
                Iterator it = singleGeneCaseValueMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    if (pair.getValue().equals("NA") || pair.getValue().equals("NaN")) {
                        it.remove();
                    }
                }

                //if it's mrna rna seq data, apply log to original values (concern of doing t-test on normal distribution)
                GeneticProfile gp = DaoGeneticProfile.getGeneticProfileById(profileId);
                if (gp.getStableId().indexOf("rna_seq") != -1) {
                    Iterator _it_log = singleGeneCaseValueMap.entrySet().iterator();
                    while (_it_log.hasNext()) {
                        Map.Entry _pair = (Map.Entry)_it_log.next();
                        _pair.setValue(Double.toString(Math.log(Double.parseDouble(_pair.getValue().toString()) + 1.0) / Math.log(2)));
                    }
                }

                String _geneName = daoGeneOptimized.getGene(_gene).getHugoGeneSymbolAllCaps();
                String _cytoband = daoGeneOptimized.getGene(_gene).getCytoband();
                
                ObjectNode _datum = mapper.createObjectNode();
                if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString())) {
                    if (!(Arrays.asList(queryGenes)).contains(_geneName)) {
                        _datum.put(COL_NAME_GENE, _geneName);
                        _datum.put(COL_NAME_CYTOBAND, _cytoband);
                        _datum.put(COL_NAME_PCT_ALTERED, calcPct(singleGeneCaseValueMap, profileType, "altered"));
                        _datum.put(COL_NAME_PCT_UNALTERED, calcPct(singleGeneCaseValueMap, profileType, "unaltered"));
                        _datum.put(COL_NAME_RATIO, calcRatio(
                                calcPct(singleGeneCaseValueMap, profileType, "altered"), calcPct(singleGeneCaseValueMap, profileType, "unaltered")));
                        _datum.put(COL_NAME_DIRECTION, "place holder"); //calculation is done by the front-end
                        _datum.put(COL_NAME_P_VALUE, calcPval(singleGeneCaseValueMap, profileType));
                        if (!(calcPct(singleGeneCaseValueMap, profileType, "altered") == 0.0 && 
                              calcPct(singleGeneCaseValueMap, profileType, "unaltered") == 0.0) &&
                              !Double.isNaN(calcPval(singleGeneCaseValueMap, profileType))) {
                            _result.add(_datum);
                        }                    
                    }
                } else if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
                    if (!(Arrays.asList(queryGenes)).contains(_geneName)) {
                        _datum.put(COL_NAME_GENE, _geneName);
                        _datum.put(COL_NAME_CYTOBAND, _cytoband);
                        _datum.put(COL_NAME_PCT_ALTERED, calcPct(singleGeneCaseValueMap, profileType, "altered"));
                        _datum.put(COL_NAME_PCT_UNALTERED, calcPct(singleGeneCaseValueMap, profileType, "unaltered"));
                        _datum.put(COL_NAME_RATIO, calcRatio(
                                calcPct(singleGeneCaseValueMap, profileType, "altered"), calcPct(singleGeneCaseValueMap, profileType, "unaltered")));
                        _datum.put(COL_NAME_DIRECTION, "place holder"); //calculation is done by the front-end
                        _datum.put(COL_NAME_P_VALUE, calcPval(singleGeneCaseValueMap, profileType));
                        if (!(calcPct(singleGeneCaseValueMap, profileType, "altered") == 0.0 && 
                             calcPct(singleGeneCaseValueMap, profileType, "unaltered") == 0.0) &&
                             !Double.isNaN(calcPval(singleGeneCaseValueMap, profileType))) {
                            _result.add(_datum);
                        }
                    }
                } else if (profileType.equals(GeneticAlterationType.MRNA_EXPRESSION.toString())) {
                    _datum.put(COL_NAME_GENE, _geneName);
                    _datum.put(COL_NAME_CYTOBAND, _cytoband);
                    _datum.put(COL_NAME_MEAN_ALTERED, calcMean(singleGeneCaseValueMap, "altered"));
                    _datum.put(COL_NAME_MEAN_UNALTERED, calcMean(singleGeneCaseValueMap, "unaltered"));
                    _datum.put(COL_NAME_STDEV_ALTERED, calcSTDev(singleGeneCaseValueMap, "altered"));
                    _datum.put(COL_NAME_STDEV_UNALTERED, calcSTDev(singleGeneCaseValueMap, "unaltered"));
                    _datum.put(COL_NAME_P_VALUE, calcPval(singleGeneCaseValueMap, profileType));
                    if (!Double.isNaN(calcPval(singleGeneCaseValueMap, profileType))) {
                        _result.add(_datum);
                    }
                } else if (profileType.equals(GeneticAlterationType.PROTEIN_LEVEL.toString())) {
                    if (proteinExpType.equals("protein")) {
                        _datum.put(COL_NAME_GENE, _geneName);
                        _datum.put(COL_NAME_CYTOBAND, _cytoband);
                        _datum.put(COL_NAME_MEAN_ALTERED, calcMean(singleGeneCaseValueMap, "altered"));
                        _datum.put(COL_NAME_MEAN_UNALTERED, calcMean(singleGeneCaseValueMap, "unaltered"));
                        _datum.put(COL_NAME_STDEV_ALTERED, calcSTDev(singleGeneCaseValueMap, "altered"));
                        _datum.put(COL_NAME_STDEV_UNALTERED, calcSTDev(singleGeneCaseValueMap, "unaltered"));
                        _datum.put(COL_NAME_P_VALUE, calcPval(singleGeneCaseValueMap, profileType));
                    } else if (proteinExpType.equals("phospho")) {
                        _datum.put(COL_NAME_GENE, _geneName);
                        _datum.put(COL_NAME_MEAN_ALTERED, calcMean(singleGeneCaseValueMap, "altered"));
                        _datum.put(COL_NAME_MEAN_UNALTERED, calcMean(singleGeneCaseValueMap, "unaltered"));
                        _datum.put(COL_NAME_STDEV_ALTERED, calcSTDev(singleGeneCaseValueMap, "altered"));
                        _datum.put(COL_NAME_STDEV_UNALTERED, calcSTDev(singleGeneCaseValueMap, "unaltered"));
                        _datum.put(COL_NAME_P_VALUE, calcPval(singleGeneCaseValueMap, profileType));
                    }
                    if (!Double.isNaN(calcPval(singleGeneCaseValueMap, profileType))) {
                        _result.add(_datum);
                    }
                }
            }

            //sort the result by p-value
            Collections.sort(_result, new pValueComparator());

            //calculate adjusted p values
            double[] originalPvalues = new double[_result.size()];
            for (int i = 0; i < _result.size(); i++) {
                originalPvalues[i] = _result.get(i).get(COL_NAME_P_VALUE).asDouble();
            }
            BenjaminiHochbergFDR bhFDR = new BenjaminiHochbergFDR(originalPvalues);
            bhFDR.calculate();
            double[] adjustedPvalues = bhFDR.getAdjustedPvalues();
            for (int j = 0; j < _result.size(); j++) {
                ((ObjectNode)_result.get(j)).put(COL_NAME_Q_VALUE, adjustedPvalues[j]);
            }

            //convert array to arraynode
            for (ObjectNode _result_node : _result) {
                result.add(_result_node);
            }
            
        } 
    }
    
    class pValueComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            ObjectNode obj1 = (ObjectNode) o1;
            ObjectNode obj2 = (ObjectNode) o2;
            if (obj1.get("p-Value").asDouble() > obj2.get("p-Value").asDouble()) return 1;
            else if (obj1.get("p-Value").asDouble() == obj2.get("p-Value").asDouble()) return 0; 
            else return -1;
        }
    }
    
    public ArrayNode getResult() {
        return result;
    }
    
    private String calcRatio(double pct1, double pct2) {
        if (pct1 != 0 && pct2 != 0) {
            if ((Math.log(pct1 / pct2) / Math.log(2)) > 10) {
                return ">10";
            } else if (Math.log(pct1 / pct2) / Math.log(2) < -10) {
                return "<-10";
            } else {
                return Double.toString(Math.log(pct1 / pct2) / Math.log(2));
            }
        } else if (pct1 == 0 && pct2 != 0) {
            return "<-10";
        } else if (pct1 != 0 && pct2 == 0) {
            return ">10";
        } else {
            return "--";
        } 
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
        
        if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString()) && copyNumType.equals("del")) {
             switch (groupType) {
                case "altered":
                    for (Integer alteredSampleId: alteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                            if (Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId)) == -2.0) { 
                                _count += 1;
                            } 
                        }
                    }
                    _result_pct = (double)(_count/alteredSampleIds.size());
                    break;
                case "unaltered":
                    for (Integer unalteredSampleId: unalteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                            if (Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId)) == -2.0) { 
                                _count += 1;
                            }   
                        }  
                    } 
                    _result_pct = (double)(_count/unalteredSampleIds.size());
                    break;
            }
        } else if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString()) && copyNumType.equals("amp")) {
            switch (groupType) {
                case "altered":
                    for (Integer alteredSampleId: alteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                            if (Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId)) == 2.0) { 
                                _count += 1;
                            } 
                        }
                    }
                    _result_pct = (double)(_count/alteredSampleIds.size());
                    break;
                case "unaltered":
                    for (Integer unalteredSampleId: unalteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                            if (Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId)) == 2.0) { 
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
        } else if (profileType.equals(GeneticAlterationType.MRNA_EXPRESSION.toString()) ||
                   profileType.equals(GeneticAlterationType.PROTEIN_LEVEL.toString())) {
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
        
        if (alteredArray.length < 2 || unalteredArray.length < 2) return Double.NaN;
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
                    switch (copyNumType) {
                        case "del":
                            if (value == -2.0) {
                                d += 1;
                            } else {
                                c += 1;
                            }   break;
                        case "amp":
                            if (value == 2.0) {
                                d += 1;
                            } else {
                                c += 1;
                            }   break;
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
                    switch (copyNumType) {
                        case "del":
                            if (value == -2.0) {
                                b += 1;
                            } else {
                                a += 1;
                            }
                        case "amp":
                            if (value == 2.0) {
                                b += 1;
                            } else {
                                a += 1;
                            }
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

