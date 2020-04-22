package org.cbioportal.service.impl;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.model.*;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CoExpressionServiceImpl implements CoExpressionService {

    @Autowired
    private MolecularDataService molecularDataService;
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
    // transaction needs to be setup here in order to return Iterable from molecularDataService in fetchCoExpressions
    @Transactional(readOnly=true)
    public List<CoExpression> getCoExpressions(String geneticEntityId, EntityType geneticEntityType,
            String sampleListId, String molecularProfileIdA, String molecularProfileIdB, Double threshold)
            throws MolecularProfileNotFoundException, SampleListNotFoundException, GenesetNotFoundException,
            GeneNotFoundException {

        if (molecularProfileIdA.equals(molecularProfileIdB)) {
            return getCoExpressions(molecularProfileIdA, sampleListId, geneticEntityId, geneticEntityType, threshold);
        }

        List<CoExpression> computedCoExpressions = null;
        List<? extends MolecularData> molecularDataListA = null;
        List<? extends MolecularData> molecularDataListB = null;
        if (geneticEntityType.equals(EntityType.GENE)) {
            molecularDataListA = molecularDataService.getMolecularData(molecularProfileIdA, sampleListId, null,
                    "SUMMARY");
        } else if (geneticEntityType.equals(EntityType.GENESET)) {
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
                finalmolecularDataListA, geneticEntityId, threshold, molecularProfileIdB);
        return computedCoExpressions;
    }

    @Override
    public List<CoExpression> getCoExpressions(String molecularProfileId, String sampleListId, String geneticEntityId,
                                               EntityType geneticEntityType, Double threshold)
        throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException {

        List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
        if (sampleIds.isEmpty()) {
            return Collections.emptyList();
        }

        return fetchCoExpressions(molecularProfileId, sampleIds, geneticEntityId, geneticEntityType, threshold);
    }

    @Override
    public List<CoExpression> fetchCoExpressions(String molecularProfileId, List<String> sampleIds, 
                                                 String queryGeneticEntityId, EntityType geneticEntityType, 
                                                 Double threshold)
        throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException {

        // For the purpose of the CoExpression computation, we separate the MolecularAlteration
        // (genetic_alteration table record) for the query gene/geneset from the MolecularAlteration(s)
        // for the remaining genes/geneset in the profile.
        MolecularAlteration queryMolecularDataList = null;
        Iterable<? extends MolecularAlteration> maItr = null;
        if (geneticEntityType.equals(EntityType.GENE)) {
            List<Integer> queryGeneticEntityIds = Arrays.asList(Integer.valueOf(queryGeneticEntityId));
            maItr = molecularDataService.getMolecularAlterations(molecularProfileId, queryGeneticEntityIds, "SUMMARY");
        } else if (geneticEntityType.equals(EntityType.GENESET)) {
            List<String> queryGeneticEntityIds = Arrays.asList(queryGeneticEntityId);
            maItr = genesetDataService.getGenesetAlterations(molecularProfileId, queryGeneticEntityIds);
        }
        for (MolecularAlteration ma : maItr) {
            queryMolecularDataList = ma;
        }
        if (queryMolecularDataList == null) {
            return Collections.emptyList();
        }

        // These next few lines are used to build a map of internal sample ids to
        // indices into the genetic_alteration.VALUES column. Recall this column
        // of the genetic_alteration table is a comma separated list of scalar values.
        // Each value in this list is associated with a sample at the same position found in
        // the genetic_profile_samples.ORDERED_SAMPLE_LIST column.
        MolecularProfileSamples commaSeparatedSampleIdsOfMolecularProfile = molecularDataRepository
            .getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
        List<Integer> internalSampleIds = Arrays.stream(commaSeparatedSampleIdsOfMolecularProfile.getSplitSampleIds())
            .mapToInt(Integer::parseInt).boxed().collect(Collectors.toList());
        Map<Integer, Integer> internalSampleIdToIndexMap = new HashMap<>();
        for (int lc = 0; lc < internalSampleIds.size(); lc++) {
            internalSampleIdToIndexMap.put(internalSampleIds.get(lc), lc);
        }

        // These next few lines build a list of Sample from the sampleIds method parameter (the user query).
        // A map is then built of internal sample ids to indices into the Sample list (although the map is
        // only used to quickly identify if a sample in the molecular profile is part of the user query - see below).
        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
        List<String> studyIds = new ArrayList<>();
        sampleIds.forEach(s -> studyIds.add(molecularProfile.getCancerStudyIdentifier()));
        List<Sample> samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
        Map<Integer, Integer> selectedSampleIdsMap = new HashMap<>();
        for (int lc = 0; lc < samples.size(); lc++) {
            selectedSampleIdsMap.put(samples.get(lc).getInternalId(), lc);
        }

        // These next few lines build a list of indices into the genetic_alteration.VALUES
        // column by iterating over all the samples in the molecular profile (method parameter)
        // and selecting only samples that are included in the user query.
        Set<Integer> includedIndexes = new HashSet<>();
        for (Integer internalSampleId : internalSampleIds) {
            if (selectedSampleIdsMap.containsKey(internalSampleId)) {
                includedIndexes.add(internalSampleIdToIndexMap.get(internalSampleId));
            }
        }

        Boolean isMolecularProfileBOfGenesetType = molecularProfile.getMolecularAlterationType()
            .equals(MolecularProfile.MolecularAlterationType.GENESET_SCORE);


        // These next few lines filter out genetic_alteration values from the query gene/geneset
        // genetic_alteration.VALUES column by considering only the indices of the samples in the user query.
        List<String> queryValues = Arrays.asList(queryMolecularDataList.getSplitValues());
        List<String> includedQueryValues = includedIndexes.stream().map(index -> queryValues.get(index))
            .collect(Collectors.toList());

        // Get an iterator to all the MolecularAlteration (genetic_alteration table records) in the profile
        if (geneticEntityType.equals(EntityType.GENE)) {
            maItr = molecularDataService.getMolecularAlterations(molecularProfileId, null, "SUMMARY");
        } else if (geneticEntityType.equals(EntityType.GENESET)) {
            maItr = genesetDataService.getGenesetAlterations(molecularProfileId, null);
        }

        // For each MolecularAlteration in the profile, compute a CoExpression to return.
        // If the MolecularAlteration is for the query gene/geneset, skip it.  Otherwise,
        // filter out genetic_alteration values from genetic_alteration.VALUES
        // by considering oly the indices of the samples in the user query.
        List<CoExpression> toReturn = new ArrayList<>();
        for (MolecularAlteration ma : maItr) {
            String entityId = ma.getStableId();
            if (entityId.equals(queryGeneticEntityId)) {
                continue;
            }
            List<String> internalValues = new ArrayList<>(Arrays.asList(ma.getSplitValues()));
            List<String> values = includedIndexes.stream().map(index -> internalValues.get(index)).collect(Collectors.toList());
            CoExpression ce = computeCoExpressions(entityId, values, includedQueryValues, isMolecularProfileBOfGenesetType, threshold, molecularProfileId);
            if (ce != null) {
                toReturn.add(ce);
            }
        }
        return toReturn;
    }

    @Override
    // transaction needs to be setup here in order to return Iterable from molecularDataService in fetchCoExpressions
    @Transactional(readOnly=true)
    public List<CoExpression> fetchCoExpressions(String geneticEntityId,
            EntityType geneticEntityType, List<String> sampleIds, String molecularProfileIdA,
            String molecularProfileIdB, Double threshold) throws MolecularProfileNotFoundException, GenesetNotFoundException, GeneNotFoundException {

        if (molecularProfileIdA.equals(molecularProfileIdB)) {
            return fetchCoExpressions(molecularProfileIdA, sampleIds, geneticEntityId, geneticEntityType, threshold);
        }

        List<CoExpression> computedCoExpressions = null;
        List<? extends MolecularData> molecularDataListA = null;
        List<? extends MolecularData> molecularDataListB = null;
        if (geneticEntityType.equals(EntityType.GENE)) {
            molecularDataListA = molecularDataService.fetchMolecularData(molecularProfileIdA, sampleIds, null,
                    "SUMMARY");
        } else if (geneticEntityType.equals(EntityType.GENESET)) {
            molecularDataListA = genesetDataService.fetchGenesetData(molecularProfileIdA, sampleIds, null);
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
                molecularDataListA, geneticEntityId, threshold, molecularProfileIdB);
        return computedCoExpressions;
    }

    private List<CoExpression> computeCoExpressionsFromMolecularData(List<? extends MolecularData> molecularDataListB,
            Boolean isMolecularProfileBOfGenesetType, List<? extends MolecularData> molecularDataListA,
            String queryGeneticEntityId, Double threshold, String molecularProfileId) throws GenesetNotFoundException, GeneNotFoundException {

        Map<String , List<MolecularData>> molecularDataMapA = molecularDataListA.stream()
            .collect(Collectors.groupingBy(MolecularData::getStableId));
        Map<String , List<MolecularData>> molecularDataMapB = molecularDataListB.stream()
            .collect(Collectors.groupingBy(MolecularData::getStableId));

        if (!molecularDataMapA.keySet().contains(queryGeneticEntityId)) {
            return Collections.emptyList();
        }

        List<? extends MolecularData> finalMolecularDataListA = (List<? extends MolecularData>)molecularDataMapA.remove(queryGeneticEntityId);
        if (molecularDataMapB.get(queryGeneticEntityId) != null) {
            List<? extends MolecularData> finalMolecularDataListB = (List<? extends MolecularData>)molecularDataMapB.remove(queryGeneticEntityId);
            if (finalMolecularDataListB == null) {
                return Collections.emptyList();
            }
        }

        List<CoExpression> coExpressionList = new ArrayList<>();

        Map<String, ? extends MolecularData> dataMapA = finalMolecularDataListA.stream()
                .collect(Collectors.toMap(MolecularData::getSampleId, Function.identity()));

        for (Entry<String, List<MolecularData>> entry : molecularDataMapB.entrySet()) {
            List<String> valuesA = new ArrayList<>();
            List<String> valuesB = new ArrayList<>();

            entry.getValue().stream().forEach(molecularData -> {
                if (dataMapA.containsKey(molecularData.getSampleId())) {
                    valuesA.add(molecularData.getValue());
                    valuesB.add(dataMapA.get(molecularData.getSampleId()).getValue());
                }
            });

            CoExpression co = computeCoExpressions(entry.getKey(), valuesA, valuesB, isMolecularProfileBOfGenesetType, threshold, molecularProfileId);
            if (co != null) {
                coExpressionList.add(co);
            }
        }

        return coExpressionList;

    }

    private CoExpression computeCoExpressions(String entityId, List<String> valuesA, List<String> valuesB, 
            Boolean isMolecularProfileBOfGenesetType, Double threshold, String molecularProfileId) throws GenesetNotFoundException, GeneNotFoundException {

        List<String> valuesACopy = new ArrayList<>(valuesA);
        List<String> valuesBCopy = new ArrayList<>(valuesB);

        List<Integer> valuesToRemove = new ArrayList<>();
        for (int i = 0; i < valuesBCopy.size(); i++) {
            if (!NumberUtils.isNumber(valuesBCopy.get(i)) || !NumberUtils.isNumber(valuesACopy.get(i))) {
                valuesToRemove.add(i);
            }
        }

        for (int i = 0; i < valuesToRemove.size(); i++) {
            int valueToRemove = valuesToRemove.get(i) - i;
            valuesBCopy.remove(valueToRemove);
            valuesACopy.remove(valueToRemove);
        }

        CoExpression coExpression = new CoExpression();
        coExpression.setGeneticEntityId(entityId);

        double[] valuesBNumber = valuesBCopy.stream().mapToDouble(Double::parseDouble).toArray();
        double[] valuesANumber = valuesACopy.stream().mapToDouble(Double::parseDouble).toArray();

        if (valuesANumber.length <= 2) {
            return null;
        }

        double[][] arrays = new double[2][valuesANumber.length];
        arrays[0] = valuesBNumber;
        arrays[1] = valuesANumber;
        SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation((new Array2DRowRealMatrix(arrays, false)).transpose());

        double spearmansValue = spearmansCorrelation.correlation(valuesBNumber, valuesANumber);
        if (Double.isNaN(spearmansValue) || Math.abs(spearmansValue) < threshold) {
            return null;
        }
        coExpression.setSpearmansCorrelation(BigDecimal.valueOf(spearmansValue));

        RealMatrix resultMatrix = spearmansCorrelation.getRankCorrelation().getCorrelationPValues();
        coExpression.setpValue(BigDecimal.valueOf(resultMatrix.getEntry(0, 1)));

        return coExpression;
    }
}
