package org.cbioportal.service.impl;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.cbioportal.model.CoExpression;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.service.CoExpressionService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
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
    private MolecularDataService molecularDataService;
    @Autowired
    private GeneService geneService;
    
    private PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
    private SpearmansCorrelation spearmansCorrelation = new SpearmansCorrelation();
    
    @Override
    public List<CoExpression> getCoExpressions(String molecularProfileId, String sampleListId, Integer entrezGeneId, 
                                              Double threshold) throws MolecularProfileNotFoundException {

        List<GeneMolecularData> molecularDataList = molecularDataService.getMolecularData(molecularProfileId, 
            sampleListId, null, "SUMMARY");
        
        return createCoExpressions(molecularDataList, entrezGeneId, threshold);
    }

    @Override
    public List<CoExpression> fetchCoExpressions(String molecularProfileId, List<String> sampleIds, Integer entrezGeneId, 
                                                Double threshold) throws MolecularProfileNotFoundException {

        List<GeneMolecularData> molecularDataList = molecularDataService.fetchMolecularData(molecularProfileId, 
            sampleIds, null, "SUMMARY");
        
        return createCoExpressions(molecularDataList, entrezGeneId, threshold);
    }
    
    private List<CoExpression> createCoExpressions(List<GeneMolecularData> molecularDataList, Integer queryEntrezGeneId,
                                                   Double threshold) {

        Map<Integer, List<GeneMolecularData>> molecularDataMap = molecularDataList.stream().filter(g -> 
            NumberUtils.isNumber(g.getValue())).collect(Collectors.groupingBy(GeneMolecularData::getEntrezGeneId));
        List<GeneMolecularData> queryMolecularDataList = molecularDataMap.remove(queryEntrezGeneId);
        
        Map<Integer, List<Gene>> genes = geneService.fetchGenes(molecularDataMap.keySet().stream()
            .map(String::valueOf).collect(Collectors.toList()), "ENTREZ_GENE_ID", "SUMMARY").stream()
            .collect(Collectors.groupingBy(Gene::getEntrezGeneId));
        
        double[] queryValues = queryMolecularDataList.stream().mapToDouble(g -> Double.parseDouble(g.getValue()))
            .toArray();
        
        List<CoExpression> coExpressionList = new ArrayList<>();
        for (Integer entrezGeneId : molecularDataMap.keySet()) {
            
            double[] values = molecularDataMap.get(entrezGeneId).stream().mapToDouble(g -> Double.parseDouble(
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
