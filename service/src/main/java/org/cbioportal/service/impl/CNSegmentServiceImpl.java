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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            chromosomes.add(getChromosome(gene));
        }
        return cnSegmentRepository.getCNSegmentData(cancerStudyId, chromosomes, sampleIds);
    }

    private String getChromosome(Gene gene) {

        String cytoband = gene.getCytoband();
        if (cytoband == null) {
            return null;
        }
        if (cytoband.toUpperCase().startsWith("X")) {
            return "X";
        }
        if (cytoband.toUpperCase().startsWith("Y")) {
            return "Y";
        }

        Pattern p = Pattern.compile("([0-9]+).*");
        Matcher m = p.matcher(cytoband);
        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

}