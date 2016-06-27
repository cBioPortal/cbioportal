package org.mskcc.cbio.portal.util;

import java.util.*;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.stats.FisherExact;

/**
 * pre-calculate/re-format input raw data based on different profiles
 * then return result (p-value) of fisher exact test
 *
 * @author suny1
 * @date Mar 16, 2015
 */
public class EnrichmentsAnalysisUtil implements DaoGeneticAlteration.AlterationProcesser {

    private String geneticProfileStableId;
    private String profileType;
    private String[] queriedGenes;
    private String copyNumType = "none";
    private List<Integer> alteredSampleIds;
    private List<Integer> unalteredSampleIds;

    private ObjectMapper mapper = new ObjectMapper();

    private final String COL_NAME_GENE = "Gene";
    private final String COL_NAME_CYTOBAND = "Cytoband";
    private final String COL_NAME_PCT_ALTERED = "percentage of alteration in altered group";
    private final String COL_NAME_PCT_UNALTERED = "percentage of alteration in unaltered group";
    private final String COL_NAME_RATIO = "Log Ratio";
    private final String COL_NAME_DIRECTION = "Direction/Tendency";
    private final String COL_NAME_P_VALUE = "p-Value";
    private final String COL_NAME_MEAN_ALTERED = "mean of alteration in altered group";
    private final String COL_NAME_MEAN_UNALTERED = "mean of alteration in unaltered group";
    private final String COL_NAME_STDEV_ALTERED = "standard deviation of alteration in altered group";
    private final String COL_NAME_STDEV_UNALTERED = "standard deviation of alteration in unaltered group";


    public EnrichmentsAnalysisUtil(

        String geneticProfileStableId,
        String profileType,
        String copyNumType,
        List<Integer> alteredSampleIds,
        List<Integer> unalteredSampleIds,
        String[] queriedGenes) throws DaoException, IllegalArgumentException {

        this.geneticProfileStableId = geneticProfileStableId;
        this.profileType = profileType;
        this.copyNumType = copyNumType;
        this.alteredSampleIds = alteredSampleIds;
        this.unalteredSampleIds = unalteredSampleIds;
        this.queriedGenes = queriedGenes;

    }

    public ObjectNode processMutHm(long entrezGeneId, ArrayList<Integer> sampleList, Map mutHm) {
        ObjectNode _datum = mapper.createObjectNode();

        //create map to pair sample and value
        HashMap<Integer, String> mapSampleValue = new HashMap<>();
        for (Integer sampleId : sampleList) { //Assign every sample (included non mutated ones) values -- mutated -> Mutation Type, non-mutated -> "Non"
            String mutationStatus = "Non";
            String tmpStr = new StringBuilder().append(Integer.toString(sampleId)).append(Long.toString(entrezGeneId)).toString();
            if (mutHm.containsKey(tmpStr)) mutationStatus = "Mutated";
            mapSampleValue.put(sampleId, mutationStatus);
        }

        //remove empty entry
        Iterator it = mapSampleValue.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (pair.getValue().equals("NA") || pair.getValue().equals("NaN") || pair.getValue().equals("null")) {
                it.remove();
            }
        }

        //get Gene Name and Cytoband
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        String geneName = daoGeneOptimized.getGene(entrezGeneId).getHugoGeneSymbolAllCaps();
        String cytoband = daoGeneOptimized.getGene(entrezGeneId).getCytoband();

        //statistics analysis
        if (!(Arrays.asList(queriedGenes)).contains(geneName)) { //remove queried genes from result
            _datum.put(COL_NAME_GENE, geneName);
            _datum.put(COL_NAME_CYTOBAND, cytoband);
            _datum.put(COL_NAME_PCT_ALTERED, Integer.toString(countAltered(mapSampleValue, profileType, "altered")) + "////" + Double.toString(calcPct(mapSampleValue, profileType, "altered")));
            _datum.put(COL_NAME_PCT_UNALTERED, Integer.toString(countAltered(mapSampleValue, profileType, "unaltered")) + "////" + Double.toString(calcPct(mapSampleValue, profileType, "unaltered")));
            _datum.put(COL_NAME_RATIO, calcRatio(
                    calcPct(mapSampleValue, profileType, "altered"), calcPct(mapSampleValue, profileType, "unaltered")));
            _datum.put(COL_NAME_DIRECTION, "place holder"); //calculation is done by the front-end
            _datum.put(COL_NAME_P_VALUE, calcPval(mapSampleValue, profileType, geneticProfileStableId));
            if (!(calcPct(mapSampleValue, profileType, "altered") == 0.0 &&
                    calcPct(mapSampleValue, profileType, "unaltered") == 0.0) &&
                    !Double.isNaN(calcPval(mapSampleValue, profileType, geneticProfileStableId))) {
                return _datum;
            }
        }

        return null;
    }

    @Override
    public ObjectNode process(long entrezGeneId, String[] values, ArrayList<Integer> sampleList) {

        ObjectNode _datum = mapper.createObjectNode();

        //create map to pair sample and value
        HashMap<Integer, String> mapSampleValue = new HashMap<>();
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            Integer sampleId = sampleList.get(i);
            mapSampleValue.put(sampleId, value);
        }

        //remove empty entry
        Iterator it = mapSampleValue.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (pair.getValue().equals("NA") || pair.getValue().equals("NaN") || pair.getValue().equals("null")) {
                it.remove();
            }
        }

        //get Gene Name and Cytoband
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        String geneName = daoGeneOptimized.getGene(entrezGeneId).getHugoGeneSymbolAllCaps();
        String cytoband = daoGeneOptimized.getGene(entrezGeneId).getCytoband();
        if (cytoband == null || cytoband.length() == 0) {
            cytoband = "--";
        } 

        //statistics analysis
        if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString())) {
            if (!(Arrays.asList(queriedGenes)).contains(geneName)) { //remove queried genes from result
                _datum.put(COL_NAME_GENE, geneName);
                _datum.put(COL_NAME_CYTOBAND, cytoband);
                _datum.put(COL_NAME_PCT_ALTERED, Integer.toString(countAltered(mapSampleValue, profileType, "altered")) + "////" + Double.toString(calcPct(mapSampleValue, profileType, "altered")));
                _datum.put(COL_NAME_PCT_UNALTERED, Integer.toString(countAltered(mapSampleValue, profileType, "unaltered")) + "////" + Double.toString(calcPct(mapSampleValue, profileType, "unaltered")));
                _datum.put(COL_NAME_RATIO, calcRatio(
                        calcPct(mapSampleValue, profileType, "altered"), calcPct(mapSampleValue, profileType, "unaltered")));
                _datum.put(COL_NAME_DIRECTION, "place holder"); //calculation is done by the front-end
                _datum.put(COL_NAME_P_VALUE, calcPval(mapSampleValue, profileType, geneticProfileStableId));
                if (!(calcPct(mapSampleValue, profileType, "altered") == 0.0 &&
                        calcPct(mapSampleValue, profileType, "unaltered") == 0.0) &&
                        !Double.isNaN(calcPval(mapSampleValue, profileType, geneticProfileStableId))) {
                    return _datum;
                }
            }
        } else if (profileType.equals(GeneticAlterationType.MRNA_EXPRESSION.toString())) {
            _datum.put(COL_NAME_GENE, geneName);
            _datum.put(COL_NAME_CYTOBAND, cytoband);
            _datum.put(COL_NAME_MEAN_ALTERED, calcMean(mapSampleValue, "altered", geneticProfileStableId));
            _datum.put(COL_NAME_MEAN_UNALTERED, calcMean(mapSampleValue, "unaltered", geneticProfileStableId));
            _datum.put(COL_NAME_STDEV_ALTERED, calcSTDev(mapSampleValue, "altered", geneticProfileStableId));
            _datum.put(COL_NAME_STDEV_UNALTERED, calcSTDev(mapSampleValue, "unaltered", geneticProfileStableId));
            _datum.put(COL_NAME_P_VALUE, calcPval(mapSampleValue, profileType, geneticProfileStableId));
            if (!Double.isNaN(calcPval(mapSampleValue, profileType, geneticProfileStableId))) {
                return _datum;
            }
        } else if (profileType.equals(GeneticAlterationType.PROTEIN_LEVEL.toString())) {
            _datum.put(COL_NAME_GENE, geneName);
            _datum.put(COL_NAME_CYTOBAND, cytoband);
            _datum.put(COL_NAME_MEAN_ALTERED, calcMean(mapSampleValue, "altered", geneticProfileStableId));
            _datum.put(COL_NAME_MEAN_UNALTERED, calcMean(mapSampleValue, "unaltered", geneticProfileStableId));
            _datum.put(COL_NAME_STDEV_ALTERED, calcSTDev(mapSampleValue, "altered", geneticProfileStableId));
            _datum.put(COL_NAME_STDEV_UNALTERED, calcSTDev(mapSampleValue, "unaltered", geneticProfileStableId));
            _datum.put(COL_NAME_P_VALUE, calcPval(mapSampleValue, profileType, geneticProfileStableId));
            if (!Double.isNaN(calcPval(mapSampleValue, profileType, geneticProfileStableId))) {
                return _datum;
            }
        }

        return null;

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

    private double calcMean(HashMap<Integer, String> singleGeneCaseValueMap, String groupType, String profileStableId) { // group type: altered or unaltered
        switch (groupType) {
            case "altered":
                int _index_altered = 0;
                double[] alteredArray = new double[alteredSampleIds.size()];
                for (Integer alteredSampleId : alteredSampleIds) {
                    if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                        if (profileStableId.indexOf("rna_seq") != -1) {
                            try {
                                alteredArray[_index_altered] = Math.log(Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId))) / Math.log(2);
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        } else {
                            try {
                                alteredArray[_index_altered] = Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId));
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        }
                        _index_altered += 1;
                    }
                }
                return StatUtils.mean(alteredArray);
            case "unaltered":
                int _index_unaltered = 0;
                double[] unalteredArray = new double[unalteredSampleIds.size()];
                for (Integer unalteredSampleId : unalteredSampleIds) {
                    if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                        if (profileStableId.indexOf("rna_seq") != -1) {
                            try {
                                unalteredArray[_index_unaltered] = Math.log(Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId))) / Math.log(2);
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        } else {
                            try {
                                unalteredArray[_index_unaltered] = Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId));
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        }
                        _index_unaltered += 1;
                    }
                }
                return StatUtils.mean(unalteredArray);
            default:
                return Double.NaN; //error
        }
    }

    private double calcSTDev(HashMap<Integer, String> singleGeneCaseValueMap, String groupType, String profileStableId) {
        switch (groupType) {
            case "altered":
                DescriptiveStatistics stats_altered = new DescriptiveStatistics();
                for (Integer alteredSampleId : alteredSampleIds) {
                    if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                        if (profileStableId.indexOf("rna_seq") != -1) {
                            try {
                                stats_altered.addValue(Math.log(Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId))) / Math.log(2));
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        } else {
                            try {
                                stats_altered.addValue(Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId)));
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        }
                    }
                }
                return stats_altered.getStandardDeviation();
            case "unaltered":
                DescriptiveStatistics stats_unaltered = new DescriptiveStatistics();
                for (Integer unalteredSampleId : unalteredSampleIds) {
                    if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                        if (profileStableId.indexOf("rna_seq") != -1) {
                            try {
                                stats_unaltered.addValue(Math.log(Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId))) / Math.log(2));
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        } else {
                            try {
                                stats_unaltered.addValue(Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId)));
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        }
                    }
                }
                return stats_unaltered.getStandardDeviation();
            default:
                return Double.NaN; //error
        }

    }

    //count alterations in different groups (altered/unaltered) (only for mutation tab and cna tab)
    private int countAltered(HashMap<Integer, String> singleGeneCaseValueMap, String profileType, String groupType) {

        int _count = 0; //altered samples count

        if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString()) && copyNumType.equals("del")) {
            switch (groupType) {
                case "altered":
                    for (Integer alteredSampleId : alteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                            try {
                                if (Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId)) == -2.0) {
                                    _count += 1;
                                }
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        }
                    }
                    break;
                case "unaltered":
                    for (Integer unalteredSampleId : unalteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                            try {
                                if (Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId)) == -2.0) {
                                    _count += 1;
                                }
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        }
                    }
                    break;
            }
        } else if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString()) && copyNumType.equals("amp")) {
            switch (groupType) {
                case "altered":
                    for (Integer alteredSampleId : alteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                            try {
                                if (Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId)) == 2.0) {
                                    _count += 1;
                                }
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        }
                    }
                    break;
                case "unaltered":
                    for (Integer unalteredSampleId : unalteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                            try {
                                if (Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId)) == 2.0) {
                                    _count += 1;
                                }
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        }
                    }
                    break;
            }
        } else if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
            switch (groupType) {
                case "altered":
                    for (Integer alteredSampleId : alteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                            if (!singleGeneCaseValueMap.get(alteredSampleId).equals("Non")) {
                                _count += 1;
                            }
                        }
                    }
                    break;
                case "unaltered":
                    for (Integer unalteredSampleId : unalteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                            if (!singleGeneCaseValueMap.get(unalteredSampleId).equals("Non")) {
                                _count += 1;
                            }
                        }
                    }
                    break;
            }
        }

        return _count;
    }

    private double calcPct(HashMap<Integer, String> singleGeneCaseValueMap, String profileType, String groupType) { // group type: altered or unaltered

        double _result_pct = 0, _count = 0; //altered samples count

        if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString()) && copyNumType.equals("del")) {
            switch (groupType) {
                case "altered":
                    for (Integer alteredSampleId : alteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                            try {
                                if (Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId)) == -2.0) {
                                    _count += 1;
                                }
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        }
                    }
                    _result_pct = _count / alteredSampleIds.size();
                    break;
                case "unaltered":
                    for (Integer unalteredSampleId : unalteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                            try {
                                if (Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId)) == -2.0) {
                                    _count += 1;
                                }
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        }
                    }
                    _result_pct = _count / unalteredSampleIds.size();
                    break;
            }
        } else if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString()) && copyNumType.equals("amp")) {
            switch (groupType) {
                case "altered":
                    for (Integer alteredSampleId : alteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                            try {
                                if (Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId)) == 2.0) {
                                    _count += 1;
                                }
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        }
                    }
                    _result_pct = _count / alteredSampleIds.size();
                    break;
                case "unaltered":
                    for (Integer unalteredSampleId : unalteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                            try {
                                if (Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId)) == 2.0) {
                                    _count += 1;
                                }
                            } catch (NumberFormatException e) {
                                e.getStackTrace();
                            }
                        }
                    }
                    _result_pct = _count / unalteredSampleIds.size();
                    break;
            }
        } else if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString())) {
            switch (groupType) {
                case "altered":
                    for (Integer alteredSampleId : alteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                            if (!singleGeneCaseValueMap.get(alteredSampleId).equals("Non")) {
                                _count += 1;
                            }
                        }
                    }
                    _result_pct = _count / alteredSampleIds.size();
                    break;
                case "unaltered":
                    for (Integer unalteredSampleId : unalteredSampleIds) {
                        if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                            if (!singleGeneCaseValueMap.get(unalteredSampleId).equals("Non")) {
                                _count += 1;
                            }
                        }
                    }
                    _result_pct = _count / unalteredSampleIds.size();
                    break;
            }
        }

        return _result_pct;

    }

    private double calcPval(HashMap<Integer, String> singleGeneCaseValueMap, String profileType, String profileStableId)
            throws IllegalArgumentException {
        double _p_value = 0.0;
        if (profileType.equals(GeneticAlterationType.MUTATION_EXTENDED.toString()) ||
                profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString())) {
            _p_value = runFisherExactTest(singleGeneCaseValueMap, profileType);
        } else if (profileType.equals(GeneticAlterationType.MRNA_EXPRESSION.toString()) ||
                profileType.equals(GeneticAlterationType.PROTEIN_LEVEL.toString())) {
            _p_value = runTTest(singleGeneCaseValueMap, profileStableId);
        }
        return _p_value;
    }

    private double runTTest(HashMap<Integer, String> singleGeneCaseValueMap, String profileStableId)
            throws IllegalArgumentException {

        double[] unalteredArray = new double[unalteredSampleIds.size()];
        double[] alteredArray = new double[alteredSampleIds.size()];
        int _index_unaltered = 0, _index_altered = 0;

        for (Integer alteredSampleId : alteredSampleIds) {
            if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                if (profileStableId.indexOf("rna_seq") != -1) {
                    try {
                        alteredArray[_index_altered] = Math.log(Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId))) / Math.log(2);
                    } catch (NumberFormatException e) {
                        e.getStackTrace();
                    }
                } else {
                    try {
                        alteredArray[_index_altered] = Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId));
                    } catch (NumberFormatException e) {
                        e.getStackTrace();
                    }
                }
                _index_altered += 1;
            }
        }
        for (Integer unalteredSampleId : unalteredSampleIds) {
            if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                if (profileStableId.indexOf("rna_seq") != -1) {
                    try {
                        unalteredArray[_index_unaltered] = Math.log(Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId))) / Math.log(2);
                    } catch (NumberFormatException e) {
                        e.getStackTrace();
                    }
                } else {
                    try {
                        unalteredArray[_index_unaltered] = Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId));
                    } catch (NumberFormatException e) {
                        e.getStackTrace();
                    }
                }
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

        for (Integer alteredSampleId : alteredSampleIds) {
            if (singleGeneCaseValueMap.containsKey(alteredSampleId)) {
                if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString())) {
                    try {
                        Double value = Double.parseDouble(singleGeneCaseValueMap.get(alteredSampleId));
                        if (copyNumType.equals("del")) {
                            if (value == -2.0) {
                                d += 1;
                            } else {
                                c += 1;
                            }
                        } else if (copyNumType.equals("amp")) {
                            if (value == 2.0) {
                                d += 1;
                            } else {
                                c += 1;
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.getStackTrace();
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

        for (Integer unalteredSampleId : unalteredSampleIds) {
            if (singleGeneCaseValueMap.containsKey(unalteredSampleId)) {
                if (profileType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION.toString())) {
                    try {
                        Double value = Double.parseDouble(singleGeneCaseValueMap.get(unalteredSampleId));
                        if (copyNumType.equals("del")) {
                            if (value == -2.0) {
                                b += 1;
                            } else {
                                a += 1;
                            }
                        } else if (copyNumType.equals("amp")) {
                            if (value == 2.0) {
                                b += 1;
                            } else {
                                a += 1;
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.getStackTrace();
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

