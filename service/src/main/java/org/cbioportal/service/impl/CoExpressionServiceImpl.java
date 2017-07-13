package org.cbioportal.service.impl;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.model.CoExpression;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneGeneticData;
import org.cbioportal.service.CoExpressionService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CoExpressionServiceImpl implements CoExpressionService {
    
    @Autowired
    private GeneticDataService geneticDataService;
    @Autowired
    private GeneService geneService;
    
    private PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
    private SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
    
    @Override
    public List<CoExpression> getCoExpressions(String geneticProfileId, String sampleListId, Integer entrezGeneId, 
                                              Double threshold) throws GeneticProfileNotFoundException {

        List<GeneGeneticData> geneticDataList = geneticDataService.getGeneticData(geneticProfileId, sampleListId, null, 
            "SUMMARY");
        
        return createCoExpressions(geneticDataList, entrezGeneId, threshold);
    }

    @Override
    public List<CoExpression> fetchCoExpressions(String geneticProfileId, List<String> sampleIds, Integer entrezGeneId, 
                                                Double threshold) throws GeneticProfileNotFoundException {

        List<GeneGeneticData> geneticDataList = geneticDataService.fetchGeneticData(geneticProfileId, sampleIds, null, 
            "SUMMARY");
        
        return createCoExpressions(geneticDataList, entrezGeneId, threshold);
    }
    
    private List<CoExpression> createCoExpressions(List<GeneGeneticData> geneticDataList, Integer queryEntrezGeneId, 
                                                  Double threshold) {

        Map<Integer, List<GeneGeneticData>> geneticDataMap = geneticDataList.stream().filter(g -> NumberUtils.isNumber(
            g.getValue())).collect(Collectors.groupingBy(GeneGeneticData::getEntrezGeneId));
        List<GeneGeneticData> queryGeneticDataList = geneticDataMap.remove(queryEntrezGeneId);
        
        Map<Integer, List<Gene>> genes = geneService.fetchGenes(geneticDataMap.keySet().stream()
            .map(String::valueOf).collect(Collectors.toList()), "ENTREZ_GENE_ID", "SUMMARY").stream()
            .collect(Collectors.groupingBy(Gene::getEntrezGeneId));
        
        double[] queryValues = queryGeneticDataList.stream().mapToDouble(g -> Double.parseDouble(g.getValue()))
            .toArray();
        
        List<CoExpression> coExpressionList = new ArrayList<>();
        for (Integer entrezGeneId : geneticDataMap.keySet()) {
            
            double[] values = geneticDataMap.get(entrezGeneId).stream().mapToDouble(g -> Double.parseDouble(
                g.getValue())).toArray();
            
            CoExpression coExpression = new CoExpression();
            coExpression.setEntrezGeneId(entrezGeneId);
            Gene gene = genes.get(entrezGeneId).get(0);
            coExpression.setCytoband(gene.getCytoband());
            coExpression.setHugoGeneSymbol(gene.getHugoGeneSymbol());
            
            double pearsonsValue = pearsonsCorrelation.correlation(queryValues, values);
            if (Double.isNaN(pearsonsValue) || Math.abs(pearsonsValue) < threshold) {
                continue;
            }
            coExpression.setPearsonsCorrelation(BigDecimal.valueOf(pearsonsValue));
            
            double spearmansValue = spearmansCorrelation.correlation(queryValues, values);
            if (Double.isNaN(spearmansValue) || Math.abs(spearmansValue) < threshold) {
                continue;
            }
            coExpression.setSpearmansCorrelation(BigDecimal.valueOf(spearmansValue));
            
            coExpressionList.add(coExpression);
        }
        
        return coExpressionList;
    }
}
