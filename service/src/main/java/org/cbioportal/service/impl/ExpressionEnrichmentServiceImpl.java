package org.cbioportal.service.impl;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.cbioportal.model.ExpressionEnrichment;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneGeneticData;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.service.ExpressionEnrichmentService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.cbioportal.service.util.BenjaminiHochbergFDRCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpressionEnrichmentServiceImpl implements ExpressionEnrichmentService {

    private static final double LOG2 = Math.log(2);
    private static final String RNA_SEQ = "rna_seq";

    @Autowired
    private SampleService sampleService;
    @Autowired
    private GeneticProfileService geneticProfileService;
    @Autowired
    private GeneticDataService geneticDataService;
    @Autowired
    private GeneService geneService;
    @Autowired
    private BenjaminiHochbergFDRCalculator benjaminiHochbergFDRCalculator;

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<ExpressionEnrichment> getExpressionEnrichments(String geneticProfileId, List<String> alteredIds, 
                                                               List<String> unalteredIds, String enrichmentType) 
        throws GeneticProfileNotFoundException {
        
        if (enrichmentType.equals("PATIENT")) {
            GeneticProfile geneticProfile = geneticProfileService.getGeneticProfile(geneticProfileId);
            alteredIds = sampleService.getAllSamplesOfPatientsInStudy(geneticProfile.getCancerStudyIdentifier(), 
                alteredIds, "ID").stream().map(Sample::getStableId).collect(Collectors.toList());
            unalteredIds = sampleService.getAllSamplesOfPatientsInStudy(geneticProfile.getCancerStudyIdentifier(),
                unalteredIds, "ID").stream().map(Sample::getStableId).collect(Collectors.toList());
        }
        
        Map<Integer, List<GeneGeneticData>> alteredGeneticDataMap = geneticDataService.fetchGeneticData(
            geneticProfileId, alteredIds, null, "SUMMARY").stream().collect(Collectors.groupingBy(
                GeneGeneticData::getEntrezGeneId));
        
        Map<Integer, List<GeneGeneticData>> unalteredGeneticDataMap = geneticDataService.fetchGeneticData(
            geneticProfileId, unalteredIds, null, "SUMMARY").stream().collect(Collectors.groupingBy(
                GeneGeneticData::getEntrezGeneId));

        Map<Integer, List<Gene>> genes = geneService.fetchGenes(alteredGeneticDataMap.keySet().stream()
            .map(String::valueOf).collect(Collectors.toList()), "ENTREZ_GENE_ID", "SUMMARY").stream()
            .collect(Collectors.groupingBy(Gene::getEntrezGeneId));

        List<ExpressionEnrichment> expressionEnrichments = new ArrayList<>();
        for (Integer entrezGeneId : alteredGeneticDataMap.keySet()) {
            
            ExpressionEnrichment expressionEnrichment = new ExpressionEnrichment();
            expressionEnrichment.setEntrezGeneId(entrezGeneId);
            Gene gene = genes.get(entrezGeneId).get(0);
            expressionEnrichment.setCytoband(gene.getCytoband());
            expressionEnrichment.setHugoGeneSymbol(gene.getHugoGeneSymbol());

            List<GeneGeneticData> alteredGeneticData = alteredGeneticDataMap.get(entrezGeneId);
            List<GeneGeneticData> unalteredGeneticData = unalteredGeneticDataMap.get(entrezGeneId);
            if (alteredGeneticData.stream().filter(a -> !NumberUtils.isNumber(a.getValue())).count() > 0 || 
                unalteredGeneticData.stream().filter(a -> !NumberUtils.isNumber(a.getValue())).count() > 0) {
                continue;
            }
            
            double[] alteredValues = getAlterationValues(alteredGeneticData, geneticProfileId);
            double[] unalteredValues = getAlterationValues(unalteredGeneticData, geneticProfileId);
            
            double alteredMean = calculateMean(alteredValues);
            double unalteredMean = calculateMean(unalteredValues);
            double alteredStandardDeviation = calculateStandardDeviation(alteredValues);
            double unalteredStandardDeviation = calculateStandardDeviation(unalteredValues);
            double pValue = calculatePValue(alteredValues, unalteredValues);
            if (Double.isNaN(alteredMean) || Double.isNaN(unalteredMean) || Double.isNaN(alteredStandardDeviation) || 
                Double.isNaN(unalteredStandardDeviation) || Double.isNaN(pValue)) {
                continue;
            }

            expressionEnrichment.setMeanExpressionInAlteredGroup(BigDecimal.valueOf(alteredMean));
            expressionEnrichment.setMeanExpressionInUnalteredGroup(BigDecimal.valueOf(unalteredMean));
            expressionEnrichment.setStandardDeviationInAlteredGroup(BigDecimal.valueOf(alteredStandardDeviation));
            expressionEnrichment.setStandardDeviationInUnalteredGroup(BigDecimal.valueOf(unalteredStandardDeviation));
            expressionEnrichment.setpValue(BigDecimal.valueOf(pValue));
            
            expressionEnrichments.add(expressionEnrichment);
        }
        
        assignQValue(expressionEnrichments);
        return expressionEnrichments;
    }
    
    private double[] getAlterationValues(List<GeneGeneticData> geneticDataList, String geneticProfileId) {
        
        if (geneticProfileId.contains(RNA_SEQ)) {
            return geneticDataList.stream().mapToDouble(g -> Math.log(Double.parseDouble(g.getValue())) / LOG2)
                .toArray();
        } else {
            return geneticDataList.stream().mapToDouble(g -> Double.parseDouble(g.getValue())).toArray();
        }
    }

    private double calculatePValue(double[] alteredValues, double[] unalteredValues) {

        return TestUtils.tTest(alteredValues, unalteredValues);
    }

    private double calculateMean(double[] values) {
        
        return StatUtils.mean(values);
    }
    
    private double calculateStandardDeviation(double[] values) {

        DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics();
        for (double value : values) {
            descriptiveStatistics.addValue(value);
        }
        return descriptiveStatistics.getStandardDeviation();
    }

    private void assignQValue(List<ExpressionEnrichment> expressionEnrichments) {

        expressionEnrichments.sort(Comparator.comparing(ExpressionEnrichment::getpValue));
        double[] qValues = benjaminiHochbergFDRCalculator.calculate(expressionEnrichments.stream().mapToDouble(a ->
            a.getpValue().doubleValue()).toArray());

        for (int i = 0; i < expressionEnrichments.size(); i++) {
            expressionEnrichments.get(i).setqValue(BigDecimal.valueOf(qValues[i]));
        }
    }
}
