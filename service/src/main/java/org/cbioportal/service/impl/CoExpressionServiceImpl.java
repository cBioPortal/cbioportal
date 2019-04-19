package org.cbioportal.service.impl;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.model.MolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.CoExpression.GeneticEntityType;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.model.CoExpression;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.service.GenesetService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.cbioportal.service.exception.GenesetNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.cbioportal.service.CoExpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CoExpressionServiceImpl implements CoExpressionService {

    @Autowired
    private MolecularDataService molecularDataService;
    @Autowired
    private GeneService geneService;
    @Autowired
    private GenesetService genesetService;
    @Autowired
    private GenesetDataService genesetDataService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private SampleListRepository sampleListRepository;
    @Autowired
    private MolecularDataRepository molecularDataRepository;
    @Autowired
    private SampleService sampleService;

    @Override
    public List<CoExpression> getCoExpressions(String molecularProfileId, String sampleListId, String geneticEntityId,
            CoExpression.GeneticEntityType geneticEntityType, Double threshold)
            throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException {

        List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
        if (sampleIds.isEmpty()) {
            return Collections.emptyList();
        }

        return fetchCoExpressions(molecularProfileId, sampleIds, geneticEntityId, geneticEntityType, threshold);
    }

    @Override
    public List<CoExpression> getCoExpressions(String geneticEntityId, CoExpression.GeneticEntityType geneticEntityType,
            String sampleListId, String molecularProfileIdA, String molecularProfileIdB, Double threshold)
            throws MolecularProfileNotFoundException, SampleListNotFoundException, GenesetNotFoundException,
            GeneNotFoundException {
        
        if (molecularProfileIdA.equals(molecularProfileIdB)) {
            return getCoExpressions(molecularProfileIdA, sampleListId, geneticEntityId, geneticEntityType, threshold);
        }

        List<CoExpression> computedCoExpressions = null;
        List<? extends MolecularData> molecularDataListA = null;
        List<? extends MolecularData> molecularDataListB = null;
        if (geneticEntityType.equals(GeneticEntityType.GENE)) {
            molecularDataListA = molecularDataService.getMolecularData(molecularProfileIdA, sampleListId, null,
                    "SUMMARY");
        } else if (geneticEntityType.equals(GeneticEntityType.GENESET)) {
            molecularDataListA = genesetDataService.fetchGenesetData(molecularProfileIdA, sampleListId, null);
        }
        MolecularProfile molecularProfileB = molecularProfileService.getMolecularProfile(molecularProfileIdB);
        Boolean isMolecularProfileBOfGenesetType = molecularProfileB.getMolecularAlterationType()
                .equals(MolecularProfile.MolecularAlterationType.GENESET_SCORE);
        if (isMolecularProfileBOfGenesetType) {
            molecularDataListB = genesetDataService.fetchGenesetData(molecularProfileIdB, sampleListId, null);
        } else {
            molecularDataListB = molecularDataService.getMolecularData(molecularProfileIdB, sampleListId, null,
                    "SUMMARY");
        }
        Set<String> samplesA = new HashSet<String>(
                (molecularDataListA.stream().map(g -> g.getSampleId()).collect(Collectors.toList())));
        Set<String> samplesB = new HashSet<String>(
                (molecularDataListB.stream().map(g -> g.getSampleId()).collect(Collectors.toList())));
        Set<String> sharedSamples = new HashSet<String>(samplesA); // use the copy constructor
        sharedSamples.retainAll(samplesB);
        List<? extends MolecularData> finalmolecularDataListA = molecularDataListA.stream()
                .filter(p -> sharedSamples.contains(p.getSampleId())).collect(Collectors.toList());
        List<? extends MolecularData> finalmolecularDataListB = molecularDataListB.stream()
                .filter(p -> sharedSamples.contains(p.getSampleId())).collect(Collectors.toList());

        computedCoExpressions = computeCoExpressionsFromMolecularData(finalmolecularDataListB, isMolecularProfileBOfGenesetType,
                finalmolecularDataListA, geneticEntityId, threshold);
        return computedCoExpressions;
    }

    @Override
    public List<CoExpression> fetchCoExpressions(String molecularProfileId, List<String> sampleIds, 
                                                 String queryGeneticEntityId, CoExpression.GeneticEntityType geneticEntityType, 
                                                 Double threshold)
        throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException {
        
        List<GeneMolecularAlteration> molecularAlterations = molecularDataService.getMolecularAlterations(
            molecularProfileId, null, "SUMMARY");

        Map<String, GeneMolecularAlteration> molecularDataMap = molecularAlterations.stream()
                .collect(Collectors.toMap(GeneMolecularAlteration::getStableId, Function.identity()));
        GeneMolecularAlteration queryMolecularDataList = molecularDataMap.remove(queryGeneticEntityId);
        
        List<CoExpression> coExpressionList = new ArrayList<>();
        if (queryMolecularDataList == null) {
            return coExpressionList;
        }

        String commaSeparatedSampleIdsOfMolecularProfile = molecularDataRepository
            .getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
        List<Integer> internalSampleIds = Arrays.stream(commaSeparatedSampleIdsOfMolecularProfile.split(","))
            .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
        Map<Integer, Integer> internalSampleIdsMap = new HashMap<>();
        for (int lc = 0; lc < internalSampleIds.size(); lc++) {
            internalSampleIdsMap.put(internalSampleIds.get(lc), lc);
        }

        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
        List<String> studyIds = new ArrayList<>();
        sampleIds.forEach(s -> studyIds.add(molecularProfile.getCancerStudyIdentifier()));
        List<Sample> samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
        Map<Integer, Integer> selectedSampleIdsMap = new HashMap<>();
        for (int lc = 0; lc < samples.size(); lc++) {
            selectedSampleIdsMap.put(samples.get(lc).getInternalId(), lc);
        }

        Set<Integer> excludedIndexes = new HashSet<>();
        for (Integer internalSampleId : internalSampleIds) {
            if (!selectedSampleIdsMap.containsKey(internalSampleId)) {
                excludedIndexes.add(internalSampleIdsMap.get(internalSampleId));
            }
        }

        Boolean isMolecularProfileBOfGenesetType = molecularProfile.getMolecularAlterationType()
                .equals(MolecularProfile.MolecularAlterationType.GENESET_SCORE);
        List<String> queryValues = Arrays.asList(queryMolecularDataList.getSplitValues());
        Map<String,List<String>> values = new HashMap<String,List<String>>();
        for (String entityId : molecularDataMap.keySet()) {
            List<String> internalValues = new ArrayList<>(Arrays.asList(molecularDataMap.get(entityId).getSplitValues()));
            values.put(entityId, internalValues);
        }
        coExpressionList = computeCoExpressions(values, queryValues, isMolecularProfileBOfGenesetType, threshold);  
        return coExpressionList;
    }

    @Override
    public List<CoExpression> fetchCoExpressions(String geneticEntityId,
            CoExpression.GeneticEntityType geneticEntityType, List<String> sampleIds, String molecularProfileIdB,
            String molecularProfileIdA, Double threshold) throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException {

        if (molecularProfileIdA.equals(molecularProfileIdB)) {
            return fetchCoExpressions(molecularProfileIdA, sampleIds, geneticEntityId, geneticEntityType, threshold);
        }

        List<CoExpression> computedCoExpressions = null;
        List<? extends MolecularData> molecularDataListA = null;
        List<? extends MolecularData> molecularDataListB = null;
        if (geneticEntityType.equals(GeneticEntityType.GENE)) {
            molecularDataListA = molecularDataService.fetchMolecularData(molecularProfileIdA, sampleIds, null,
                    "SUMMARY");
        } else if (geneticEntityType.equals(GeneticEntityType.GENESET)) {
            molecularDataListA = genesetDataService.fetchGenesetData(molecularProfileIdB, sampleIds, null);
        }
        MolecularProfile molecularProfileB = molecularProfileService.getMolecularProfile(molecularProfileIdB);
        Boolean isMolecularProfileBOfGenesetType = molecularProfileB.getMolecularAlterationType()
                .equals(MolecularProfile.MolecularAlterationType.GENESET_SCORE);
        if (isMolecularProfileBOfGenesetType) {
            molecularDataListB = genesetDataService.fetchGenesetData(molecularProfileIdB, sampleIds, null).stream()
                    .collect(Collectors.toList());
            ;
        } else {
            molecularDataListB = molecularDataService
                    .fetchMolecularData(molecularProfileIdB, sampleIds, null, "SUMMARY").stream()
                    .collect(Collectors.toList());
        }
        computedCoExpressions = computeCoExpressionsFromMolecularData(molecularDataListB, isMolecularProfileBOfGenesetType,
                molecularDataListA, geneticEntityId, threshold);
        return computedCoExpressions;
    }

    private List<CoExpression> computeCoExpressionsFromMolecularData(List<? extends MolecularData> molecularDataListB,
            Boolean isMolecularProfileBOfGenesetType, List<? extends MolecularData> molecularDataListA,
            String queryGeneticEntityId, Double threshold) throws GenesetNotFoundException, GeneNotFoundException 
                                                     {
        
        Map<String , List<MolecularData>> molecularDataMapA = molecularDataListA.stream()
            .collect(Collectors.groupingBy(MolecularData::getStableId));
        Map<String , List<MolecularData>> molecularDataMapB = molecularDataListB.stream()
            .collect(Collectors.groupingBy(MolecularData::getStableId));
        
        List<CoExpression> coExpressionList = new ArrayList<>();
        
        if (!molecularDataMapA.keySet().contains(queryGeneticEntityId)) {
            return coExpressionList;
        }

        List<? extends MolecularData> finalMolecularDataListA = (List<? extends MolecularData>)molecularDataMapA.remove(queryGeneticEntityId);
        if (molecularDataMapB.get(queryGeneticEntityId) != null) {
            List<? extends MolecularData> finalMolecularDataListB = (List<? extends MolecularData>)molecularDataMapB.remove(queryGeneticEntityId);
            if (finalMolecularDataListB == null) {
                return coExpressionList;
            }
        }

        Map<String,List<String>> values = new HashMap<String,List<String>>();
        for (String entityId : molecularDataMapB.keySet()) {
            List<String> internalValues = molecularDataMapB.get(entityId).stream().map(g -> g.getValue())
                .collect(Collectors.toList());
            values.put(entityId, internalValues);
        }
        List<String> valuesB = finalMolecularDataListA.stream().map(g -> g.getValue()).collect(Collectors.toList());
        coExpressionList = computeCoExpressions(values, valuesB, isMolecularProfileBOfGenesetType, threshold);

        return coExpressionList;

    }

    private List<CoExpression> computeCoExpressions(Map<String, List<String>> valuesA, List<String> valuesB, 
            Boolean isMolecularProfileBOfGenesetType, Double threshold) throws GenesetNotFoundException, GeneNotFoundException {
        
        
        List<CoExpression> coExpressionList = new ArrayList<>();
        for (String entityId : valuesA.keySet()) {
            List<String> values = valuesA.get(entityId);
            List<String> valuesBCopy = new ArrayList<>(valuesB);

            List<Integer> valuesToRemove = new ArrayList<>();
            for (int i = 0; i < valuesBCopy.size(); i++) {
                if (!NumberUtils.isNumber(valuesBCopy.get(i)) || !NumberUtils.isNumber(values.get(i))) {
                    valuesToRemove.add(i);
                }
            }

            for (int i = 0; i < valuesToRemove.size(); i++) {
                int valueToRemove = valuesToRemove.get(i) - i;
                valuesBCopy.remove(valueToRemove);
                values.remove(valueToRemove);
            }
            
            CoExpression coExpression = new CoExpression();
            coExpression.setGeneticEntityId(entityId);
            if (isMolecularProfileBOfGenesetType) {
                Geneset geneset = genesetService.getGeneset(entityId);
                coExpression.setCytoband("-");
                coExpression.setGeneticEntityName(geneset.getName());
            } else {
                Gene gene = geneService.getGene(entityId);
                coExpression.setCytoband(gene.getCytoband());
                coExpression.setGeneticEntityName(gene.getHugoGeneSymbol());
            }
            

            double[] valuesBNumber = valuesBCopy.stream().mapToDouble(Double::parseDouble).toArray();
            double[] valuesNumber = values.stream().mapToDouble(Double::parseDouble).toArray();

            if (valuesNumber.length <= 2) {
                continue;
            }
            
            double[][] arrays = new double[2][valuesNumber.length];
            arrays[0] = valuesBNumber;
            arrays[1] = valuesNumber;
            SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation((new Array2DRowRealMatrix(arrays, false)).transpose());

            double spearmansValue = spearmansCorrelation.correlation(valuesBNumber, valuesNumber);
            if (Double.isNaN(spearmansValue) || Math.abs(spearmansValue) < threshold) {
                continue;
            }
            coExpression.setSpearmansCorrelation(BigDecimal.valueOf(spearmansValue));

            RealMatrix resultMatrix = spearmansCorrelation.getRankCorrelation().getCorrelationPValues();
            coExpression.setpValue(BigDecimal.valueOf(resultMatrix.getEntry(0, 1)));
            
            coExpressionList.add(coExpression);
        }
        
        return coExpressionList;
    }
}
