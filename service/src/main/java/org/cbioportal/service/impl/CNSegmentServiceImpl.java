/**
 *
 * @author jiaojiao
 */
package org.cbioportal.service.impl;

import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import org.cbioportal.model.CNSegmentData;
import org.cbioportal.model.Gene;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.persistence.CNSegmentRepository;
import org.cbioportal.service.CNSegmentService;

@Service
public class CNSegmentServiceImpl implements CNSegmentService {

    @Autowired
    private CNSegmentRepository cnSegmentRepository;
    
    @Autowired
    private GeneRepository geneRepository;

    @Override
    public List<CNSegmentData> getCNSegmentData(String cancerStudyId, List<String> hugoGeneSymbols, List<String> sampleIds) {
        Set<String> chromosomes = new HashSet<>();
        List<Gene> genes = geneRepository.getGeneListByHugoSymbols(hugoGeneSymbols);
        for(Gene gene : genes){
            chromosomes.add(gene.getChromosome());
        }
        return cnSegmentRepository.getCNSegmentData(cancerStudyId, chromosomes, sampleIds);
    }

}