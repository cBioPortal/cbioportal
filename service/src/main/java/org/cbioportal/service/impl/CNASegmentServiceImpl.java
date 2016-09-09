/**
 *
 * @author jiaojiao
 */
package org.cbioportal.service.impl;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import org.cbioportal.model.CNASegmentData;
import org.cbioportal.model.Gene;
import org.cbioportal.persistence.CNASegmentRepository;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.service.CNASegmentService;

@Service
public class CNASegmentServiceImpl implements CNASegmentService {

    @Autowired
    private CNASegmentRepository cnaSegmentRepository;
    
    @Autowired
    private GeneRepository geneRepository;

    public List<CNASegmentData> getCNASegmentData(String cancerStudyId, List<String> hugoGeneSymbols, List<String> sampleIds) {
        List<String> chromosomes = new ArrayList<String>();
        List<Gene> genes = geneRepository.getGeneListByHugoSymbols(hugoGeneSymbols);
        for(Gene gene : genes){
            chromosomes.add(gene.getChromosome());
        }
        return cnaSegmentRepository.getCNASegmentData(cancerStudyId, chromosomes, sampleIds);
    }

}