package org.cbioportal.service.impl;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.model.CoExpression;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.CoExpressionService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
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
    private SampleListRepository sampleListRepository;
    @Autowired
    private MolecularDataRepository molecularDataRepository;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private SampleService sampleService;
    
    @Override
    public List<CoExpression> getCoExpressions(String molecularProfileId, String sampleListId, Integer entrezGeneId, 
                                              Double threshold) throws MolecularProfileNotFoundException {

        List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
        if (sampleIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        return fetchCoExpressions(molecularProfileId, sampleIds, entrezGeneId, threshold);
    }

    @Override
    public List<CoExpression> fetchCoExpressions(String molecularProfileId, List<String> sampleIds, 
                                                 Integer queryEntrezGeneId, Double threshold) 
        throws MolecularProfileNotFoundException {
        
        List<GeneMolecularAlteration> molecularAlterations = molecularDataService.getMolecularAlterations(
            molecularProfileId, null, "SUMMARY");

        Map<Integer, GeneMolecularAlteration> molecularDataMap = molecularAlterations.stream()
                .collect(Collectors.toMap(GeneMolecularAlteration::getEntrezGeneId, Function.identity()));
        GeneMolecularAlteration queryMolecularDataList = molecularDataMap.remove(queryEntrezGeneId);

        Map<Integer, List<Gene>> genes = geneService.fetchGenes(molecularDataMap.keySet().stream()
            .map(String::valueOf).collect(Collectors.toList()), "ENTREZ_GENE_ID", "SUMMARY").stream()
            .collect(Collectors.groupingBy(Gene::getEntrezGeneId));
        
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

        List<String> queryValues = Arrays.asList(queryMolecularDataList.getSplitValues());
        for (Integer entrezGeneId : molecularDataMap.keySet()) {
            
            List<String> values = new ArrayList<>(Arrays.asList(molecularDataMap.get(entrezGeneId).getSplitValues()));
            List<String> queryValuesCopy = new ArrayList<>(queryValues);

            List<Integer> valuesToRemove = new ArrayList<>();
            for (int i = 0; i < queryValuesCopy.size(); i++) {
                if (!NumberUtils.isNumber(queryValuesCopy.get(i)) || !NumberUtils.isNumber(values.get(i)) 
                    || excludedIndexes.contains(i)) {
                    valuesToRemove.add(i);
                }
            }

            for (int i = 0; i < valuesToRemove.size(); i++) {
                int valueToRemove = valuesToRemove.get(i) - i;
                queryValuesCopy.remove(valueToRemove);
                values.remove(valueToRemove);
            }
            
            CoExpression coExpression = new CoExpression();
            coExpression.setEntrezGeneId(entrezGeneId);
            Gene gene = genes.get(entrezGeneId).get(0);
            coExpression.setCytoband(gene.getCytoband());
            coExpression.setHugoGeneSymbol(gene.getHugoGeneSymbol());

            double[] queryValuesNumber = queryValuesCopy.stream().mapToDouble(Double::parseDouble).toArray();
            double[] valuesNumber = values.stream().mapToDouble(Double::parseDouble).toArray();

            if (valuesNumber.length <= 2) {
                continue;
            }
            
            double[][] arrays = new double[2][valuesNumber.length];
            arrays[0] = queryValuesNumber;
            arrays[1] = valuesNumber;
            SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation((new Array2DRowRealMatrix(arrays, false)).transpose());

            double spearmansValue = spearmansCorrelation.correlation(queryValuesNumber, valuesNumber);
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
