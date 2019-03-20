package org.cbioportal.service.impl;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.model.CoExpression;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.service.CoExpressionService;
import org.cbioportal.service.GeneService;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.model.Sample;
import org.cbioportal.service.SampleService;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CoExpressionServiceImpl implements CoExpressionService {

    @Autowired
    private MolecularDataRepository molecularDataRepository;
    @Autowired
    private MolecularDataService molecularDataService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private GeneService geneService;
    @Autowired
    private SampleListRepository sampleListRepository;
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
    public List<CoExpression> fetchCoExpressions(String molecularProfileId, List<String> sampleIds, Integer entrezGeneId, 
                                                Double threshold) throws MolecularProfileNotFoundException {
        
        List<GeneMolecularAlteration> molecularDataList = molecularDataService.getMolecularAlterations(molecularProfileId, null, "SUMMARY");

        String commaSeparatedSampleIdsOfMolecularProfile = molecularDataRepository
            .getCommaSeparatedSampleIdsOfMolecularProfile(molecularProfileId);
        
        String[] sampleIdsInAlterationOrder = commaSeparatedSampleIdsOfMolecularProfile.split(",");
        
        MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileId);
        List<String> studyIds = new ArrayList<>();
        sampleIds.forEach(s -> studyIds.add(molecularProfile.getCancerStudyIdentifier()));
        List<Sample> samples = sampleService.fetchSamples(studyIds, sampleIds, "ID");
        List<Integer> internalSampleIds = new ArrayList<>();
        for (Sample s: samples) {
            internalSampleIds.add(s.getInternalId());
        }
        
        return createCoExpressions(molecularDataList, entrezGeneId, internalSampleIds, sampleIdsInAlterationOrder, threshold);
    }
    
    private List<CoExpression> createCoExpressions(List<GeneMolecularAlteration> molecularAlterationList, 
                                                   Integer queryEntrezGeneId,
                                                   List<Integer> internalSampleIds,
                                                   String[] sampleIdsInAlterationOrder,
                                                   Double threshold) {

        Map<Integer, GeneMolecularAlteration> molecularAlterationMap = molecularAlterationList.stream()
                .collect(Collectors.toMap(GeneMolecularAlteration::getEntrezGeneId, Function.identity()));
        GeneMolecularAlteration queryGeneMolecularAlteration = molecularAlterationMap.remove(queryEntrezGeneId);

        Map<Integer, List<Gene>> genes = geneService.fetchGenes(molecularAlterationMap.keySet().stream()
            .map(String::valueOf).collect(Collectors.toList()), "ENTREZ_GENE_ID", "SUMMARY").stream()
            .collect(Collectors.groupingBy(Gene::getEntrezGeneId));
        
        List<CoExpression> coExpressionList = new ArrayList<>();

        if (queryGeneMolecularAlteration == null) {
            return coExpressionList;
        }

        String[] queryValues = queryGeneMolecularAlteration.getSplitValues();
        
        Map<String, Integer> sampleIdToIndex = new HashMap<>();
        for (int i=0; i<sampleIdsInAlterationOrder.length; i++) {
            sampleIdToIndex.put(sampleIdsInAlterationOrder[i], i);
        }
        
        int[] sampleIndexes = new int[internalSampleIds.size()];
        int sampleIndexesIndex = 0;
        for (Integer sampleId: internalSampleIds) {
            if (!sampleIdToIndex.containsKey(sampleId.toString())) {
                // TODO: throw error?
                continue;
            }
            sampleIndexes[sampleIndexesIndex] = sampleIdToIndex.get(sampleId.toString());
            sampleIndexesIndex += 1;
        }
        
        
        List<Integer> queryValuesToRemove = new ArrayList<>();
        for (Integer i: sampleIndexes) {
            if (!NumberUtils.isNumber(queryValues[i])) {
                queryValuesToRemove.add(i);
            }
        }
        
        for (Integer entrezGeneId : molecularAlterationMap.keySet()) {
            
            String[] values = molecularAlterationMap.get(entrezGeneId).getSplitValues();

            // array of indexes of samples with invalid values
            Set<Integer> valuesToRemove = new HashSet<>();
            for (Integer i: sampleIndexes) {
                if (!NumberUtils.isNumber(values[i])) {
                    valuesToRemove.add(i);
                }
            }
            for (Integer i: queryValuesToRemove) {
                valuesToRemove.add(i);
            }
            
            CoExpression coExpression = new CoExpression();
            coExpression.setEntrezGeneId(entrezGeneId);
            Gene gene = genes.get(entrezGeneId).get(0);
            coExpression.setCytoband(gene.getCytoband());
            coExpression.setHugoGeneSymbol(gene.getHugoGeneSymbol());
            
            int validDataCount = sampleIndexes.length - valuesToRemove.size();

            if (validDataCount <= 2) {
                continue;
            }
            
            double[][] arrays = new double[2][validDataCount];
            int dataIndex = 0;
            for (Integer i: sampleIndexes) {
                if (valuesToRemove.contains(i)) {
                    continue; // skip invalid data index
                }
                arrays[0][dataIndex] = Double.parseDouble(queryValues[i]);
                arrays[1][dataIndex] = Double.parseDouble(values[i]);
                dataIndex += 1;
            }

            SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation((new Array2DRowRealMatrix(arrays, false)).transpose());

            double spearmansValue = spearmansCorrelation.correlation(arrays[0], arrays[1]);
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
