package org.cbioportal.service.impl;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.model.MolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.CoExpression.GeneticEntityType;
import org.cbioportal.model.CoExpression;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.service.GenesetService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.CoExpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
    @Override
    public List<CoExpression> getCoExpressions(String geneticEntityId, CoExpression.GeneticEntityType geneticEntityType, String sampleListId, 
                                              String molecularProfileIdA, String molecularProfileIdB, Double threshold) throws Exception {
        
        List<CoExpression> computedCoExpressions = null;
        List<? extends MolecularData>  molecularDataListA = null;
        List<? extends MolecularData>  molecularDataListB = null;
        if (geneticEntityType.equals(GeneticEntityType.GENE)) {
            molecularDataListA = molecularDataService.getMolecularData(molecularProfileIdA, sampleListId, null, "SUMMARY");
        } else if (geneticEntityType.equals(GeneticEntityType.GENESET)) {
            molecularDataListA = genesetDataService.fetchGenesetData(molecularProfileIdA, sampleListId, null);
        }
        MolecularProfile molecularProfileB = molecularProfileService.getMolecularProfile(molecularProfileIdB);
        Boolean isMolecularProfileBOfGenesetType = molecularProfileB.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.GENESET_SCORE);
        if (isMolecularProfileBOfGenesetType) {
            molecularDataListB = genesetDataService.fetchGenesetData(molecularProfileIdB, sampleListId, null);
        } else {
            molecularDataListB = molecularDataService.getMolecularData(molecularProfileIdB, sampleListId, null, "SUMMARY");
        }
        Set<String> samplesA = new HashSet<String>((molecularDataListA.stream().map(g -> g.getSampleId()).collect(Collectors.toList())));
        Set<String> samplesB = new HashSet<String>((molecularDataListB.stream().map(g -> g.getSampleId()).collect(Collectors.toList())));
        Set<String> sharedSamples = new HashSet<String>(samplesA); // use the copy constructor
        sharedSamples.retainAll(samplesB);
        List<? extends MolecularData> finalmolecularDataListA = molecularDataListA.stream().filter(p -> sharedSamples.contains(p.getSampleId()))
                .collect(Collectors.toList());
        List<? extends MolecularData> finalmolecularDataListB = molecularDataListB.stream().filter(p -> sharedSamples.contains(p.getSampleId()))
                .collect(Collectors.toList());

        computedCoExpressions = computeCoExpressions(finalmolecularDataListB, isMolecularProfileBOfGenesetType, finalmolecularDataListA, geneticEntityId, threshold);
        return computedCoExpressions;
    }

    @Override
    public List<CoExpression> fetchCoExpressions(String geneticEntityId, CoExpression.GeneticEntityType geneticEntityType, List<String> sampleIds, 
                                              String molecularProfileIdB, String molecularProfileIdA, Double threshold) throws Exception {

        List<CoExpression> computedCoExpressions = null;
        List<? extends MolecularData>  molecularDataListA = null;
        List<? extends MolecularData>  molecularDataListB = null;
        if (geneticEntityType.equals(GeneticEntityType.GENE)) {
            molecularDataListA = molecularDataService.fetchMolecularData(molecularProfileIdA, sampleIds, null, "SUMMARY");
        } else if (geneticEntityType.equals(GeneticEntityType.GENESET)) {
            molecularDataListA = genesetDataService.fetchGenesetData(molecularProfileIdB, sampleIds, null);
        }
        MolecularProfile molecularProfileB = molecularProfileService.getMolecularProfile(molecularProfileIdB);
        Boolean isMolecularProfileBOfGenesetType = molecularProfileB.getMolecularAlterationType().equals(MolecularProfile.MolecularAlterationType.GENESET_SCORE);
        if (isMolecularProfileBOfGenesetType) {
            molecularDataListB = genesetDataService.fetchGenesetData(molecularProfileIdB, sampleIds, null).stream().collect(Collectors.toList());;
        } else {
            molecularDataListB = molecularDataService.fetchMolecularData(molecularProfileIdB, sampleIds, null, "SUMMARY").stream().collect(Collectors.toList());
        }
        computedCoExpressions = computeCoExpressions(molecularDataListB, isMolecularProfileBOfGenesetType, molecularDataListA, geneticEntityId, threshold);
        return computedCoExpressions;
    }
    
    private List<CoExpression> computeCoExpressions(List<? extends MolecularData> molecularDataListB, Boolean isMolecularProfileBOfGenesetType,
                                                    List<? extends MolecularData> molecularDataListA, String queryGeneticEntityId, Double threshold) 
                                                    throws Exception {
        
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

        List<String> valuesB = finalMolecularDataListA.stream().map(g -> g.getValue()).collect(Collectors.toList());
        for (String entityId : molecularDataMapB.keySet()) {
            
            List<String> values = molecularDataMapB.get(entityId).stream().map(g -> g.getValue())
                .collect(Collectors.toList());
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
            
            double[][] arrays = new double[valuesNumber.length][2];
            for (int i = 0; i < valuesNumber.length; i++) {
                arrays[i][0] = valuesBNumber[i];
                arrays[i][1] = valuesNumber[i];
            }

            SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation(new Array2DRowRealMatrix(arrays, false));

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
